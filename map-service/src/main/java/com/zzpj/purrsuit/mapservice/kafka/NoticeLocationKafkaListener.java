package com.zzpj.purrsuit.mapservice.kafka;

import com.zzpj.purrsuit.common.events.NoticeLocationEvent;
import com.zzpj.purrsuit.mapservice.service.LocationMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoticeLocationKafkaListener {

    private final LocationMatchingService locationMatchingService;

    @KafkaListener(topics = "notice-location-topic", groupId = "map-service-group")
    public void consumeNoticeLocation(NoticeLocationEvent event) {
        log.info("Odebrano lokację dla Notice ID: {}", event.noticeId());

        // Przekazanie danych do istniejącego LocationMatchingService
        // Zostawiamy accuracyRadiusMeters jako null lub podajemy wartość domyślną
        locationMatchingService.save(
                event.noticeId(),
                event.noticeType(),
                event.latitude(),
                event.longitude(),
                null
        );
    }
}