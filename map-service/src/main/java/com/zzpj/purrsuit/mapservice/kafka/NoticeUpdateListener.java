package com.zzpj.purrsuit.mapservice.kafka;

import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.common.events.NoticeStatusUpdateEvent;
import com.zzpj.purrsuit.mapservice.repository.GeoLocationRepository;
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
    private final GeoLocationRepository repository;

    /**
     * Konsumuje informacje o zmianie statusu ogłoszenia z tematu "notice-update-map".
     * Wykorzystywane do aktualizacji stanu lokalnego rekordu zgłoszenia.
     *
     * @param event zdarzenie zmiany statusu
     */
    @KafkaListener(
            topics = "notice-update-map",
            groupId = "map-service-group"
    )
    public void updateNotice(NoticeStatusUpdateEvent event){
        log.info("Status zgłoszenia: {}, został zmieniony", event.noticeId());

        try {
             repository.updateStatusByNoticeId(event.noticeId(), NoticeStatus.valueOf(event.newStatus()));

        }catch (Exception e){
            log.error("Błąd podczas przetwarzania wiadomości z notice-service dla ogłoszenia {}", event.noticeId(), e);
        }
    }
}
