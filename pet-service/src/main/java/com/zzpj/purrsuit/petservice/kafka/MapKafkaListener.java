package com.zzpj.purrsuit.petservice.kafka;

import com.zzpj.purrsuit.petservice.event.MapMatchEvent; // Twój rekord przeniesiony do pet-service
import com.zzpj.purrsuit.petservice.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapKafkaListener {

    private final MatchingService matchingService; // Twój serwis z

    @KafkaListener(topics = "nearby-notices-topic", groupId = "pet-matching-group")
    public void consumeNearbyNotices(MapMatchEvent event) {
        log.info("Odebrano event! Zgłoszenie zgubienia: {}, potencjalne znalezione w okolicy: {}",
                event.lostNoticeId(), event.foundNotices());

        try {
            // Przykładowe wywołanie Twojej metody
            matchingService.processLocationMatchEvent(event.lostNoticeId(), event.foundNotices());
        } catch (Exception e) {
            log.error("Błąd podczas przetwarzania dopasowań dla zgłoszenia {}", event.lostNoticeId(), e);
        }
    }
}