package com.zzpj.purrsuit.petservice.kafka;

import com.zzpj.purrsuit.common.events.MatchFoundNoticeEvent;
import com.zzpj.purrsuit.common.events.MatchResultEvent;
import com.zzpj.purrsuit.petservice.entity.MatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producent wiadomości Kafka powiadamiający powiązane serwisy
 * o odnalezieniu prawdopodobnego dopasowania.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchFoundNoticeProducer {
    private final KafkaTemplate<String, MatchFoundNoticeEvent> kafkaTemplate;
    private static final String TOPIC_NOTICE = "match-found-notice";
    private static final String TOPIC_MAP = "match-found-map";

    /**
     * Wysyła powiadomienia o udanym dopasowaniu na tematy "match-found-notice"
     * oraz "match-found-map".
     *
     * @param result potwierdzony wynik dopasowania do rozgłoszenia
     */
    public void sendFoundMatchNotice(MatchResult result){
        MatchFoundNoticeEvent event = new MatchFoundNoticeEvent(
                result.getLostNoticeId(),
                result.getSeenNoticeId(),
                result.getSimilarityScore()
        );
        log.info("Publishing match found notice to map-service and notice-service  for Lost: {}, Seen: {}",
                 event.lostNoticeId(), event.seenNoticeId());

        kafkaTemplate.send(TOPIC_NOTICE,event.lostNoticeId().toString(),event);
        kafkaTemplate.send(TOPIC_MAP,event.lostNoticeId().toString(),event);

    }
}

