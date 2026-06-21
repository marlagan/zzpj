package com.zzpj.purrsuit.petservice.kafka;

import com.zzpj.purrsuit.common.events.NoticeCreatedEvent;
import com.zzpj.purrsuit.petservice.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Nasłuchuje zdarzeń o nowo dodanych ogłoszeniach do systemu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeCreatedListener {

    private final MatchingService matchingService;

    /**
     * Odbiera informacje o nowym ogłoszeniu z tematu "notice-activated",
     * co inicjuje zapis w lokalnej bazie i proces szukania dopasowań.
     *
     * @param event zdarzenie utworzenia ogłoszenia zawierające jego szczegóły
     */
    @KafkaListener(
            topics = "notice-activated",
            groupId = "pet-notice-group"
    )
    public void consumeNewNotice(NoticeCreatedEvent event) {
        log.info("Odebrano nowe ogłoszenie z notice-service! Użytkownik:{}, ID: {}, Gatunek: {}, Typ: {}, Opis: {}",
                event.userId(), event.noticeId(), event.species(), event.type(), event.description());
        try {
            matchingService.handleIncomingNotice(event);
        } catch (Exception e) {
            log.error("Błąd podczas przetwarzania wiadomości z notice-service dla ogłoszenia {}", event.noticeId(), e);
        }
    }
}