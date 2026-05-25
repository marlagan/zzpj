package com.zzpj.purrsuit.petservice.service;
import com.zzpj.purrsuit.petservice.entity.PetNotice;
import com.zzpj.purrsuit.petservice.event.NoticeCreatedEvent;
import com.zzpj.purrsuit.petservice.kafka.MatchResultProducer;
import com.zzpj.purrsuit.petservice.model.MatchResult;
import com.zzpj.purrsuit.petservice.enums.MatchStatus;
import com.zzpj.purrsuit.petservice.repository.MatchResultRepository;
import com.zzpj.purrsuit.petservice.repository.PetNoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {
    private final PetNoticeRepository petNoticeRepository;
    private final SemanticMatchService semanticMatchService;
    private final MatchResultRepository matchResultRepository;
    private final MatchResultProducer matchResultProducer;

    @Value("${matching.similarity.threshold:0.75}")
    private double threshold;


    public void handleIncomingNotice(NoticeCreatedEvent event) {
        log.info("Zapisywanie ogłoszenia z Kafki i szukanie dopasowań: {}", event.noticeId());

        // 1. Zapisz nowe ogłoszenie LOKALNIE w pet-service
        PetNotice newNotice = PetNotice.builder()
                .noticeId(event.noticeId())
                .type(event.type())
                .species(event.species())
                .description(event.description())
                .build();
        petNoticeRepository.save(newNotice);


        String oppositeType = event.type().equalsIgnoreCase("LOST") ? "FOUND" : "LOST";
        List<PetNotice> candidates = petNoticeRepository.findByTypeAndSpecies(oppositeType, event.species());

        if (candidates.isEmpty()) {
            log.info("Brak lokalnych kandydatów ({} - {}) dla ogłoszenia: {}", oppositeType, event.species(), event.noticeId());
            return;
        }


        candidates.forEach(candidate -> {
            double score = semanticMatchService.comparePetDescription(
                    newNotice.getDescription(),
                    candidate.getDescription()
            );

            log.info("Wynik AI {} <-> {}: {}", newNotice.getNoticeId(), candidate.getNoticeId(), score);

            if (score >= threshold) {

                boolean isNewLost = newNotice.getType().equalsIgnoreCase("LOST");
                UUID lostId = isNewLost ? newNotice.getNoticeId() : candidate.getNoticeId();
                UUID foundId = isNewLost ? candidate.getNoticeId() : newNotice.getNoticeId();

                MatchResult result = MatchResult.builder()
                        .lostNoticeId(lostId)
                        .seenNoticeId(foundId)
                        .similarityScore(score)
                        .status(MatchStatus.PENDING)
                        .build();

                matchResultRepository.save(result);

                matchResultProducer.sendMatchNotification(result);
            }
        });
    }

    public void processLocationMatchEvent(UUID sourceNoticeId, List<UUID> nearbyNoticeIds) {
        log.info("Analiza zbieżności lokalizacji dla ogłoszenia {} ({} potencjalnych sąsiadów)",
                sourceNoticeId, nearbyNoticeIds.size());

        if (nearbyNoticeIds.isEmpty()) {
            return;
        }

        Optional<PetNotice> sourceNoticeOpt = petNoticeRepository.findById(sourceNoticeId);

        if (sourceNoticeOpt.isEmpty()) {
            log.warn("Ogłoszenie {} nie istnieje w lokalnej bazie pet-service. Pomijam dopasowanie lokalizacyjne.", sourceNoticeId);
            return;
        }

        PetNotice sourceNotice = sourceNoticeOpt.get();

        List<PetNotice> candidates = petNoticeRepository.findAllById(nearbyNoticeIds).stream()
                .filter(candidate -> candidate.getSpecies().equalsIgnoreCase(sourceNotice.getSpecies()))
                .filter(candidate -> !candidate.getType().equalsIgnoreCase(sourceNotice.getType()))
                .toList();

        if (candidates.isEmpty()) {
            log.info("Żaden z sąsiadów nie jest odpowiednim kandydatem (niezgodny gatunek lub typ) dla {}", sourceNoticeId);
            return;
        }

        log.info("Po filtracji lokalnej pozostało {} kandydatów do oceny semantycznej dla {}", candidates.size(), sourceNoticeId);

        candidates.forEach(candidate -> {
            UUID lostId = sourceNotice.getType().equalsIgnoreCase("LOST") ? sourceNotice.getNoticeId() : candidate.getNoticeId();
            UUID seenId = sourceNotice.getType().equalsIgnoreCase("LOST") ? candidate.getNoticeId() : sourceNotice.getNoticeId();

            if (matchResultRepository.findByLostNoticeIdAndSeenNoticeId(lostId, seenId).isPresent()) {
                log.info("Dopasowanie {} <-> {} już istnieje w bazie. Pomijam.", lostId, seenId);
                return;
            }

            MatchResult result = scoreCandidate(sourceNotice, candidate);

            if (result.getSimilarityScore() >= threshold) {
                log.info("Znaleziono silne dopasowanie lokalizacyjne + semantyczne (Score: {})! Wysyłanie powiadomienia...", result.getSimilarityScore());
                matchResultProducer.sendMatchNotification(result);
            }
        });
    }

    private MatchResult scoreCandidate(PetNotice query, PetNotice candidate) {
        double score = semanticMatchService.comparePetDescription(
                query.getDescription(),
                candidate.getDescription()
        );

        log.info("Wynik podobieństwa {} <-> {}: {}", query.getNoticeId(), candidate.getNoticeId(), score);


        boolean isQueryLost = query.getType().equalsIgnoreCase("LOST");
        UUID lostId = isQueryLost ? query.getNoticeId() : candidate.getNoticeId();
        UUID seenId = isQueryLost ? candidate.getNoticeId() : query.getNoticeId();

        MatchResult result = MatchResult.builder()
                .lostNoticeId(lostId)
                .seenNoticeId(seenId)
                .similarityScore(score)
                .status(MatchStatus.PENDING)
                .build();

        return matchResultRepository.save(result);
    }
    public List<MatchResult> getMatchesForNotice(UUID noticeID) {
        return matchResultRepository.findByLostNoticeId(noticeID);
    }

    /**
     * Metoda do pobierania szczegółów konkretnego dopasowania.
     */
    public Optional<MatchResult> getMatchDetail(UUID lostNoticeId, UUID seenNoticeId) {
        return matchResultRepository.findByLostNoticeIdAndSeenNoticeId(lostNoticeId, seenNoticeId);
    }
}
