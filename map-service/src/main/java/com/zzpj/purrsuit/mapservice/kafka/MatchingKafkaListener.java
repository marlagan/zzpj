package com.zzpj.purrsuit.mapservice.kafka;

import com.zzpj.purrsuit.common.events.MatchFoundNoticeEvent;
import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.mapservice.repository.GeoLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingKafkaListener {
    private final GeoLocationRepository repository;

    @Transactional // Dodaj tę adnotację
    @KafkaListener(topics = "match-found-map", groupId = "map-service-group")
    public void consumeMatchEvent(MatchFoundNoticeEvent event) {
        log.info("Otrzymano MatchFoundNoticeEvent z Kafki. Aktualizacja statusów dla lost={}, seen={}",
                 event.lostNoticeId(), event.seenNoticeId());

        try {
            repository.updateStatusByNoticeId(event.lostNoticeId(), NoticeStatus.PENDING_MATCH);
            repository.updateStatusByNoticeId(event.seenNoticeId(), NoticeStatus.PENDING_MATCH);
            log.info("Pomyślnie zaktualizowano status obu lokalizacji na PENDING_MATCH.");
        } catch (Exception e) {
            log.error("Wystąpił błąd podczas zmiany statusu dla eventu dopasowania: {}", e.getMessage(), e);
        }
    }

}
