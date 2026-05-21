package com.zzpj.purrsuit.noticeservice.service;

import com.zzpj.purrsuit.noticeservice.client.MapServiceClient;
import com.zzpj.purrsuit.noticeservice.domain.NoticeStatus;
import com.zzpj.purrsuit.noticeservice.domain.NoticeType;
import com.zzpj.purrsuit.noticeservice.dto.NoticeDto.*;
import com.zzpj.purrsuit.noticeservice.entity.Notice;
import com.zzpj.purrsuit.noticeservice.kafka.NoticeEventProducer;
import com.zzpj.purrsuit.noticeservice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private static final int SRID = 4326;
    private final GeometryFactory gf = new GeometryFactory(new PrecisionModel(), SRID);

    private final NoticeRepository     noticeRepository;
    private final AnimalVisionService  visionService;
    private final NoticeEventProducer  eventProducer;
    private final MapServiceClient     mapServiceClient;

    // ── Tworzenie zgłoszenia LOST / FOUND ─────────────────────────────────────

    /**
     * Tworzy zgłoszenie i — jeśli dołączono zdjęcie — uruchamia analizę AI.
     * Zwraca opis AI do weryfikacji. Status = PENDING_AI_REVIEW.
     * Gdy brak zdjęcia: od razu ACTIVE.
     */
    @Transactional
    public NoticeResponse createNotice(CreateNoticeRequest req) {
        Notice notice = Notice.builder()
                .type(req.type)
                .reportedByUserId(req.reportedByUserId)
                .species(req.species)
                .breed(req.breed)
                .colorDescription(req.colorDescription)
                .additionalNotes(req.additionalNotes)
                .photoUrl(req.photoUrl)
                .location(toPoint(req.latitude, req.longitude))
                .eventDate(req.eventDate)
                .build();

        // Groq generuje opis z danych formularza (niezależnie od zdjęcia)
        String aiDesc = visionService.generateDescription(
                req.species, req.breed, req.colorDescription, req.additionalNotes);
        if (aiDesc != null) {
            notice.setAiGeneratedDescription(aiDesc);
            // status pozostaje PENDING_AI_REVIEW — użytkownik musi potwierdzić
        } else {
            // Groq niedostępny — aktywuj od razu bez opisu AI
            notice.setAiDescriptionConfirmed(true);
            notice.setStatus(NoticeStatus.ACTIVE);
        }

        notice = noticeRepository.save(notice);
        log.info("Notice created id={} status={}", notice.getId(), notice.getStatus());

        if (notice.getStatus() == NoticeStatus.ACTIVE) {
            publishAndRegisterLocation(notice);
        }

        return toResponse(notice);
    }

    // ── Potwierdzenie opisu AI ────────────────────────────────────────────────

    /**
     * Użytkownik zatwierdza lub poprawia opis AI → status ACTIVE.
     * Publikuje event Kafka "notice-activated" do pet-service.
     * Rejestruje lokalizację w map-service.
     */
    @Transactional
    public NoticeResponse confirmAiDescription(UUID noticeId, ConfirmAiDescriptionRequest req) {
        Notice notice = findById(noticeId);

        if (notice.getStatus() != NoticeStatus.PENDING_AI_REVIEW) {
            throw new IllegalStateException(
                    "Notice " + noticeId + " nie czeka na potwierdzenie opisu (status=" + notice.getStatus() + ")");
        }

        notice.setAiGeneratedDescription(req.confirmedDescription);
        notice.setAiDescriptionConfirmed(true);
        notice.setStatus(NoticeStatus.ACTIVE);
        notice = noticeRepository.save(notice);

        log.info("AI description confirmed → ACTIVE, noticeId={}", noticeId);
        publishAndRegisterLocation(notice);

        return toResponse(notice);
    }

    // ── Tworzenie obserwacji (SIGHTING) ───────────────────────────────────────

    /**
     * Ktoś widział zwierzę — zgłasza obserwację powiązaną z istniejącym LOST/FOUND.
     * Sighting wchodzi od razu jako ACTIVE.
     * Opcjonalne zdjęcie → opis AI (informacyjnie, nie blokuje aktywacji).
     */
    @Transactional
    public NoticeResponse createSighting(CreateSightingRequest req) {
        Notice parent = findById(req.parentNoticeId);
        if (parent.getType() == NoticeType.SIGHTING) {
            throw new IllegalArgumentException("Nie można zgłosić obserwacji obserwacji.");
        }

        Notice sighting = Notice.builder()
                .type(NoticeType.SIGHTING)
                .status(NoticeStatus.ACTIVE)
                .reportedByUserId(req.reportedByUserId)
                .species(parent.getSpecies())
                .breed(parent.getBreed())
                .additionalNotes(req.additionalNotes)
                .photoUrl(req.photoUrl)
                .parentNoticeId(req.parentNoticeId)
                .location(toPoint(req.latitude, req.longitude))
                .eventDate(req.eventDate)
                .aiDescriptionConfirmed(true)
                .build();

        // Dla sightingu opis AI generujemy z danych rodzica + notatek obserwatora
        if (req.additionalNotes != null && !req.additionalNotes.isBlank()) {
            sighting.setAiGeneratedDescription(
                    visionService.generateDescription(
                            parent.getSpecies(), parent.getBreed(),
                            parent.getColorDescription(), req.additionalNotes));
        }

        sighting = noticeRepository.save(sighting);
        log.info("Sighting created id={} parentId={}", sighting.getId(), req.parentNoticeId);

        // Kafka do pet-service + rejestracja lokalizacji
        publishAndRegisterLocation(sighting);

        return toResponse(sighting);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public NoticeResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<NoticeResponse> getByUser(UUID userId) {
        return noticeRepository.findByReportedByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Używane przez pet-service: GET /api/notices?type=LOST&status=ACTIVE
     * Parametr "confirmed=true" w stubie pet-service przekładamy na status=ACTIVE.
     */
    @Transactional(readOnly = true)
    public List<NoticeResponse> getByTypeAndStatus(NoticeType type, NoticeStatus status) {
        return noticeRepository.findByTypeAndStatus(type, status)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NoticeResponse> getSightingsForNotice(UUID parentNoticeId) {
        return noticeRepository.findByParentNoticeId(parentNoticeId)
                .stream().map(this::toResponse).toList();
    }

    // ── Status update (wywoływany przez pet-service przez Kafka lub REST) ─────

    @Transactional
    public NoticeResponse updateStatus(UUID id, NoticeStatus newStatus) {
        Notice notice = findById(id);
        notice.setStatus(newStatus);
        return toResponse(noticeRepository.save(notice));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Notice findById(UUID id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notice not found: " + id));
    }

    private Point toPoint(double lat, double lon) {
        return gf.createPoint(new Coordinate(lon, lat)); // JTS: x=lon, y=lat
    }

    /**
     * Opis przekazywany do pet-service jako "description".
     * Priorytet: aiGeneratedDescription → colorDescription → additionalNotes → species.
     */
    private String resolveDescription(Notice n) {
        if (n.getAiGeneratedDescription() != null && !n.getAiGeneratedDescription().isBlank())
            return n.getAiGeneratedDescription();
        if (n.getColorDescription() != null && !n.getColorDescription().isBlank())
            return n.getColorDescription();
        if (n.getAdditionalNotes() != null && !n.getAdditionalNotes().isBlank())
            return n.getAdditionalNotes();
        return n.getSpecies();
    }

    private void publishAndRegisterLocation(Notice n) {
        // 1. Kafka event → pet-service
        var event = eventProducer.buildEvent(
                n.getId(), n.getType(), n.getReportedByUserId(),
                n.getSpecies(), n.getBreed(),
                resolveDescription(n),
                n.getLocation().getY(), n.getLocation().getX(),
                n.getEventDate());

        if (n.getType() == NoticeType.SIGHTING) {
            eventProducer.sendSightingCreated(event);
        } else {
            eventProducer.sendNoticeActivated(event);
        }

        // 2. Rejestracja lokalizacji w map-service
        mapServiceClient.saveLocation(
                n.getId(),
                n.getType().name(),
                n.getLocation().getY(),
                n.getLocation().getX(),
                null);
    }

    NoticeResponse toResponse(Notice n) {
        return NoticeResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .status(n.getStatus())
                .reportedByUserId(n.getReportedByUserId())
                .species(n.getSpecies())
                .breed(n.getBreed())
                .colorDescription(n.getColorDescription())
                .additionalNotes(n.getAdditionalNotes())
                .photoUrl(n.getPhotoUrl())
                .aiGeneratedDescription(n.getAiGeneratedDescription())
                .aiDescriptionConfirmed(n.isAiDescriptionConfirmed())
                .parentNoticeId(n.getParentNoticeId())
                .latitude(n.getLocation() != null ? n.getLocation().getY() : null)
                .longitude(n.getLocation() != null ? n.getLocation().getX() : null)
                .eventDate(n.getEventDate())
                .createdAt(n.getCreatedAt())
                .build();
    }
}