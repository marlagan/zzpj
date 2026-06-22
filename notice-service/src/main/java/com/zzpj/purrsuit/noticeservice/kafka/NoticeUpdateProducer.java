package com.zzpj.purrsuit.noticeservice.kafka;

import com.zzpj.purrsuit.common.events.NoticeStatusUpdateEvent;
import com.zzpj.purrsuit.common.events.NoticeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Temat: "notice-update"
// Konsument: pet-service (NoticeUpdateListener) — aktualizuje status
// dopasowania w MatchingService po zmianie statusu zgłoszenia.

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeUpdateProducer {

    static final String TOPIC_NOTICE_UPDATE = "notice-update";

    private final KafkaTemplate<String, NoticeStatusUpdateEvent> kafkaNoticeUpdateTemplate;

    public void sendNoticeStatusUpdate(UUID noticeId, NoticeStatus newStatus) {
        try {
            var event = new NoticeStatusUpdateEvent(noticeId, newStatus.toString());
            kafkaNoticeUpdateTemplate.send(TOPIC_NOTICE_UPDATE, noticeId.toString(), event);
            log.info("Kafka [{}] key={} newStatus={}", TOPIC_NOTICE_UPDATE, noticeId, newStatus);
        } catch (Exception e) {
            log.error("Failed to publish to Kafka topic={} key={}", TOPIC_NOTICE_UPDATE, noticeId, e);
        }
    }
}