package com.zzpj.purrsuit.noticeservice.kafka;

import com.zzpj.purrsuit.common.events.MatchResultEvent;
import com.zzpj.purrsuit.noticeservice.service.NoticeMatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Konsumuje topic "pet-match-results", na który pet-service publikuje
 * MatchResultEvent po znalezieniu dopasowania (semantycznego lub
 * lokalizacyjnego) pomiędzy zgłoszeniem LOST i FOUND.
 *
 * Po odebraniu eventu: tworzona jest nowa encja NoticeMatch (status PENDING),
 * a oba powiązane zgłoszenia przechodzą w status PENDING_MATCH — do czasu,
 * aż użytkownik potwierdzi lub odrzuci dopasowanie
 * (NoticeMatchController / NoticeMatchService).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchResultKafkaListener {

    private final NoticeMatchService noticeMatchService;

    @KafkaListener(topics = "pet-match-results", groupId = "notice-match-group")
    public void consumeMatchResult(MatchResultEvent event) {
        log.info("Odebrano wynik dopasowania z pet-service: lost={}, seen={}, score={}",
                event.lostNoticeId(), event.seenNoticeId(), event.similarityScore());
        try {
            noticeMatchService.handleMatchFound(event);
        } catch (Exception e) {
            log.error("Błąd podczas przetwarzania dopasowania lost={} seen={}",
                    event.lostNoticeId(), event.seenNoticeId(), e);
        }
    }
}
