package com.zzpj.purrsuit.petservice.kafka;

import com.zzpj.purrsuit.common.events.NoticeStatusUpdateEvent;
import com.zzpj.purrsuit.petservice.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Nasłuchuje zdarzeń o aktualizacji statusu ogłoszeń.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeUpdateListener {
    private final MatchingService matchingService;

    /**
     * Konsumuje informacje o zmianie statusu ogłoszenia z tematu "notice-update".
     * Wykorzystywane do aktualizacji stanu lokalnego rekordu zgłoszenia.
     *
     * @param event zdarzenie zmiany statusu
     */
    @KafkaListener(
            topics = "notice-update",
            groupId = "pet-notice-group"
    )
    public void updateNotice(NoticeStatusUpdateEvent event){
        log.info("Status zgłoszenia: {}, został zmieniony", event.noticeId());

        try {
            matchingService.updateNoticeStatus(event);

        }catch (Exception e){
            log.error("Błąd podczas przetwarzania wiadomości z notice-service dla ogłoszenia {}", event.noticeId(), e);
        }
    }
}
