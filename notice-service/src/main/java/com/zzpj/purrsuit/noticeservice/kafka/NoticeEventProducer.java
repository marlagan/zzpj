package com.zzpj.purrsuit.noticeservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzpj.purrsuit.noticeservice.domain.NoticeType;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

// Event 1: dla pet-service
// Temat: "notice-activated"
// Konsument: pet-service — szuka dopasowań na podstawie gatunku i opisu (Groq LLM)

@Data
@Builder
class NoticeDescriptionEvent {
    private UUID noticeId;
    private String species;       // gatunek (np. "kot")
    private String description;   // aiGeneratedDescription lub colorDescription
}

// Event 2: dla map-service
// Temat: "notice-location"
// Konsument: map-service — rejestruje lokalizację, szuka ogłoszeń w promieniu

@Data
@Builder
class NoticeLocationEvent {
    private UUID noticeId;
    private NoticeType noticeType;   // LOST | FOUND
    private double latitude;         // z Point.getY()
    private double longitude;        // z Point.getX()
    private LocalDateTime createdAt;
}

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeEventProducer {

    static final String TOPIC_DESCRIPTION = "notice-activated";
    static final String TOPIC_LOCATION    = "notice-location";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Event 1 → pet-service
     * Przekazuje noticeId, gatunek i opis — pet-service porównuje opisy
     * przez Groq LLM i szuka dopasowań (score ≥ 0.75).
     */
    public void sendDescriptionEvent(UUID noticeId, String species, String description) {
        var event = NoticeDescriptionEvent.builder()
                .noticeId(noticeId)
                .species(species)
                .description(description)
                .build();
        send(TOPIC_DESCRIPTION, noticeId.toString(), event);
    }

    /**
     * Event 2 → map-service
     * Przekazuje noticeId, typ, lokalizację (Point rozłożony na lat/lon)
     * i datę utworzenia — map-service rejestruje punkt i wykonuje
     * zapytania ST_DWithin w PostGIS.
     */
    public void sendLocationEvent(UUID noticeId, NoticeType noticeType,
                                  Point location, LocalDateTime createdAt) {
        var event = NoticeLocationEvent.builder()
                .noticeId(noticeId)
                .noticeType(noticeType)
                .latitude(location.getY())
                .longitude(location.getX())
                .createdAt(createdAt)
                .build();
        send(TOPIC_LOCATION, noticeId.toString(), event);
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
