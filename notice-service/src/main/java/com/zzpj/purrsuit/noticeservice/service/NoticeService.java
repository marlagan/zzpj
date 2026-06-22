package com.zzpj.purrsuit.noticeservice.service;

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

    private final NoticeRepository    noticeRepository;
    private final AnimalVisionService visionService;
    private final NoticeEventProducer eventProducer;

    /**
     * Tworzy zgłoszenie LOST lub FOUND.
     * Groq generuje opis z danych formularza → zapisywany w aiGeneratedDescription.
     * Ogłoszenie od razu wchodzi jako ACTIVE.
     * Po zapisie: Kafka event → pet-service + rejestracja lokalizacji w map-service.
     *
     * @param reportedByUserId UUID zalogowanego użytkownika pobrany z tokenu JWT
     *                         (NoticeController#createNotice) — nigdy z requestu klienta.
     */
    @Transactional
    public NoticeResponse createNotice(CreateNoticeRequest req, UUID reportedByUserId) {
        Notice notice = Notice.builder()
                .type(req.type)
                .reportedByUserId(reportedByUserId)
                .species(req.species)
                .breed(req.breed)
                .colorDescription(req.colorDescription)
                .additionalNotes(req.additionalNotes)
                .photoUrl(req.photoUrl)
                .location(toPoint(req.latitude, req.longitude))
                .eventDate(req.eventDate)
                .build();

        String aiDesc = visionService.generateDescription(
                req.species, req.breed, req.colorDescription, req.additionalNotes);
        notice.setAiGeneratedDescription(aiDesc);

        notice = noticeRepository.save(notice);
        log.info("Notice created id={} type={}", notice.getId(), notice.getType());

        publishAndRegisterLocation(notice);

        return toResponse(notice);
    }

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
     */
    @Transactional(readOnly = true)
    public List<NoticeResponse> getByTypeAndStatus(NoticeType type, NoticeStatus status) {
        return noticeRepository.findByTypeAndStatus(type, status)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Wywoływany przez pet-service gdy znajdzie match (PENDING_MATCH)
     * lub przez użytkownika (RESOLVED, CLOSED).
     */
    @Transactional
    public NoticeResponse updateStatus(UUID id, NoticeStatus newStatus) {
        Notice notice = findById(id);
        notice.setStatus(newStatus);
        return toResponse(noticeRepository.save(notice));
    }

    private Notice findById(UUID id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notice not found: " + id));
    }

    private Point toPoint(double lat, double lon) {
        return gf.createPoint(new Coordinate(lon, lat)); // JTS: x=lon, y=lat
    }

    /**
     * Priorytet opisu do pet-service:
     * aiGeneratedDescription → colorDescription → additionalNotes → species
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
        // Event 1 → pet-service: noticeId + gatunek + opis
        eventProducer.sendDescriptionEvent(
                n.getId(),
                n.getReportedByUserId(),
                n.getSpecies(),
                resolveDescription(n),
                n.getType());

        // Event 2 → map-service: noticeId + typ + lokalizacja + data utworzenia
        eventProducer.sendLocationEvent(
                n.getId(),
                n.getType(),
                n.getLocation(),
                n.getCreatedAt());
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
                .latitude(n.getLocation() != null ? n.getLocation().getY() : null)
                .longitude(n.getLocation() != null ? n.getLocation().getX() : null)
                .eventDate(n.getEventDate())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
