package com.zzpj.purrsuit.noticeservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzpj.purrsuit.noticeservice.domain.NoticeType;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event publikowany po aktywacji ogłoszenia (confirm-description)
 * oraz po utworzeniu sightingu.
 *
 * Używa StringSerializer (tak jak reszta serwisów w projekcie).
 * Obiekt serializowany do JSON przez ObjectMapper → String.
 */
@Data
@Builder
class NoticeActivatedEvent {
    private UUID noticeId;
    private NoticeType type;
    private UUID reportedByUserId;
    private String species;
    private String breed;
    private String description;   // aiGeneratedDescription lub colorDescription
    private Double latitude;
    private Double longitude;
    private LocalDateTime eventDate;
    private LocalDateTime activatedAt;
}

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeEventProducer {

    static final String TOPIC_NOTICE_ACTIVATED = "notice-activated";
    static final String TOPIC_SIGHTING_CREATED  = "sighting-created";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendNoticeActivated(NoticeActivatedEvent event) {
        send(TOPIC_NOTICE_ACTIVATED, event.getNoticeId().toString(), event);
    }

    public void sendSightingCreated(NoticeActivatedEvent event) {
        send(TOPIC_SIGHTING_CREATED, event.getNoticeId().toString(), event);
    }

    NoticeActivatedEvent buildEvent(
            UUID noticeId, NoticeType type, UUID userId,
            String species, String breed, String description,
            Double lat, Double lon,
            LocalDateTime eventDate) {
        return NoticeActivatedEvent.builder()
                .noticeId(noticeId)
                .type(type)
                .reportedByUserId(userId)
                .species(species)
                .breed(breed)
                .description(description)
                .latitude(lat)
                .longitude(lon)
                .eventDate(eventDate)
                .activatedAt(LocalDateTime.now())
                .build();
    }

    private void send(String topic, String key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, key, json);
            log.info("Kafka [{}] key={}", topic, key);
        } catch (Exception e) {
            log.error("Failed to publish to Kafka topic={} key={}", topic, key, e);
        }
    }
}
