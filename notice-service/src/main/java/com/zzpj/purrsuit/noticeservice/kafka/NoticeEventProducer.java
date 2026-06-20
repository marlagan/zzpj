package com.zzpj.purrsuit.noticeservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzpj.purrsuit.common.events.NoticeCreatedEvent;
import com.zzpj.purrsuit.common.events.NoticeLocationEvent;
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

// Event 2: dla map-service
// Temat: "notice-location"
// Konsument: map-service — rejestruje lokalizację, szuka ogłoszeń w promieniu

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeEventProducer {

    static final String TOPIC_DESCRIPTION = "notice-activated";
    static final String TOPIC_LOCATION    = "notice-location-topic";

    private final KafkaTemplate<String, NoticeLocationEvent> kafkaMapTemplate;
    private final KafkaTemplate<String, NoticeCreatedEvent> kafkaDescriptionTemplate;

    /**
     * Event 1 → pet-service
     * Przekazuje noticeId, gatunek i opis — pet-service porównuje opisy
     * przez Groq LLM i szuka dopasowań (score ≥ 0.75).
     */
    public void sendDescriptionEvent(UUID noticeId, UUID reportedByUserId,String species, String description, NoticeType type) {
        try {
            var event = new NoticeCreatedEvent(noticeId, reportedByUserId, species, description, type.toString());
            kafkaDescriptionTemplate.send(TOPIC_DESCRIPTION, noticeId.toString(), event);
            log.info("Kafka [{}] key={}", TOPIC_DESCRIPTION, noticeId);
        } catch (Exception e) {
            log.error("Failed to publish to Kafka topic={} key={}", TOPIC_DESCRIPTION, noticeId, e);
        }
    }

    /**
     * Event 2 → map-service
     * Przekazuje noticeId, typ, lokalizację (Point rozłożony na lat/lon)
     * i datę utworzenia — map-service rejestruje punkt i wykonuje
     * zapytania ST_DWithin w PostGIS.
     */
    public void sendLocationEvent(UUID noticeId, NoticeType noticeType,
                                  Point location, LocalDateTime createdAt) {
        try {
            var event = new NoticeLocationEvent(noticeId, noticeType.toString(), location.getY(), location.getX(), createdAt);
            kafkaMapTemplate.send(TOPIC_LOCATION, noticeId.toString(), event);
            log.info("Kafka [{}] key={}", TOPIC_LOCATION, noticeId);
        } catch (Exception e) {
            log.error("Failed to publish to Kafka topic={} key={}", TOPIC_LOCATION, noticeId, e);
        }
    }

    private void send(String topic, String key, Object payload) {

    }
}
