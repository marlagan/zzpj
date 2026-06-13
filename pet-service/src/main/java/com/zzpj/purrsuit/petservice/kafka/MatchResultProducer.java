package com.zzpj.purrsuit.petservice.kafka;

import com.zzpj.purrsuit.common.events.MatchResultEvent;
import com.zzpj.purrsuit.petservice.model.MatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchResultProducer {
    private final KafkaTemplate<String,MatchResultEvent> kafkaTemplate;
    private static final String TOPIC = "pet-match-results";

    public void sendMatchNotification(MatchResult result){
        MatchResultEvent event = new MatchResultEvent(
                result.getLostNoticeId(),
                result.getSeenNoticeId(),
                result.getSimilarityScore()
        );
        log.info("Publishing match result to Kafka topic '{}' for Lost: {}, Seen: {}",
                TOPIC, event.lostNoticeId(), event.seenNoticeId());

        kafkaTemplate.send(TOPIC, event.lostNoticeId().toString(), event);
    }
}
