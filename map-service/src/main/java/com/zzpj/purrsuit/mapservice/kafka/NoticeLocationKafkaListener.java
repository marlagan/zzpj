package com.zzpj.purrsuit.mapservice.kafka;

import com.zzpj.purrsuit.common.events.NoticeLocationEvent;
import com.zzpj.purrsuit.mapservice.service.LocationMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Nasłuchiwacz odpowiedzialny za przyjmowanie nowych danych lokalizacyjnych.
 * Pozwala na asynchroniczne zasilanie mapy nowymi zgłoszeniami tworzonymi
 * w innych częściach systemu (np. w głównym systemie zgłoszeń użytkowników).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NoticeLocationKafkaListener {

    private final LocationMatchingService locationMatchingService;

    /**
     * Odbiera z Kafki zdarzenie utworzenia nowego ogłoszenia, które posiada współrzędne GPS.
     * Następnie deleguje logikę do {@link LocationMatchingService}, który zapisuje punkt na mapie
     * i automatycznie triggeruje szukanie dopasowań w okolicy.
     *
     * @param event Zdarzenie utworzenia nowej lokalizacji.
     */
    @KafkaListener(topics = "notice-location-topic", groupId = "map-service-group")
    public void consumeNoticeLocation(NoticeLocationEvent event) {
        log.info("Odebrano lokację dla Notice ID: {}", event.noticeId());

        // Przekazanie danych do istniejącego LocationMatchingService
        // Zostawiamy accuracyRadiusMeters jako null lub podajemy wartość domyślną
        locationMatchingService.save(
                event.noticeId(),
                event.noticeType(),
                event.species(),
                event.latitude(),
                event.longitude(),
                null,
                event.noticeStatus()
        );
    }
}