package com.zzpj.purrsuit.noticeservice.kafka;

import com.zzpj.purrsuit.noticeservice.entity.NoticeMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchDecisionProducer {

    static final String TOPIC_CONFIRMED = "match-confirmed-topic";
    static final String TOPIC_REJECTED = "match-rejected-topic";

    private final KafkaTemplate<String, MatchDecisionEvent> kafkaTemplate;

    /**
     * Wysyłane po zatwierdzeniu odnalezienia zwierzęcia.
     * Odbiorcy: map-service, pet-service.
     */
    public void notifyMatchConfirmed(NoticeMatch match) {
        send(TOPIC_CONFIRMED, toEvent(match));
    }

    /**
     * Wysyłane po odrzuceniu dopasowania.
     * Odbiorcy: map-service, pet-service.
     */
    public void notifyMatchRejected(NoticeMatch match) {
        send(TOPIC_REJECTED, toEvent(match));
    }

    private void send(String topic, MatchDecisionEvent event) {
        try {
            kafkaTemplate.send(topic, event.matchId().toString(), event);
            log.info("Kafka [{}] matchId={} lost={} seen={}",
                    topic, event.matchId(), event.lostNoticeId(), event.seenNoticeId());
        } catch (Exception e) {
            log.error("Failed to publish to Kafka topic={} matchId={}", topic, event.matchId(), e);
        }
    }

    private MatchDecisionEvent toEvent(NoticeMatch match) {
        return new MatchDecisionEvent(
                match.getId(),
                match.getLostNoticeId(),
                match.getSeenNoticeId(),
                match.getStatus().name(),
                match.getDecidedAt());
    }
}
