package com.zzpj.purrsuit.petservice.kafka;

import com.zzpj.purrsuit.common.events.NearbyNoticesEvent;
import com.zzpj.purrsuit.petservice.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapKafkaListener {

    private final MatchingService matchingService;

    @KafkaListener(topics = "nearby-notices-topic", groupId = "pet-matching-group")
    public void consumeNearbyNotices(NearbyNoticesEvent event) {
        log.info("Odebrano event! Zgłoszenie zgubienia: {}, potencjalne znalezione w okolicy: {}",
                event.lostNoticeId(), event.nearbyFoundNoticeIds());

        try {

            matchingService.processLocationMatchEvent(event.lostNoticeId(), event.nearbyFoundNoticeIds());
        } catch (Exception e) {
            log.error("Błąd podczas przetwarzania dopasowań dla zgłoszenia {}", event.lostNoticeId(), e);
        }
    }
}