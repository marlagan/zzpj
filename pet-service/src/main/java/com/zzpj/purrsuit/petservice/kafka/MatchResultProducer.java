package com.zzpj.purrsuit.petservice.kafka;

import com.zzpj.purrsuit.common.events.MatchResultEvent;
import com.zzpj.purrsuit.petservice.entity.MatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producent wiadomości Kafka odpowiedzialny za rozgłaszanie wyników dopasowań.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchResultProducer {
    private final KafkaTemplate<String,MatchResultEvent> kafkaTemplate;
    private static final String TOPIC = "pet-match-results";

    /**
     * Publikuje wyliczony wynik dopasowania na temat "pet-match-results",
     * aby inne mikroserwisy mogły zareagować na znalezione powiązanie.
     *
     * @param result encja zawierająca pełne dane wyniku dopasowania
     */
    public void sendMatchNotification(MatchResult result){
        MatchResultEvent event = new MatchResultEvent(
                result.getLostNoticeId(),
                result.getSeenNoticeId(),
                result.getLostOwnerId(),
                result.getSimilarityScore()
        );
        log.info("Publishing match result to Kafka topic '{}' for Lost: {}, Seen: {}",
                TOPIC, event.lostNoticeId(), event.seenNoticeId());

        kafkaTemplate.send(TOPIC, event.lostNoticeId().toString(), event);
    }
}
