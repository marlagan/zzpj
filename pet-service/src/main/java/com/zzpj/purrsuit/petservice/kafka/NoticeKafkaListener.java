package com.zzpj.purrsuit.petservice.kafka;

import com.zzpj.purrsuit.common.events.NoticeCreatedEvent;
import com.zzpj.purrsuit.petservice.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeKafkaListener {

    private final MatchingService matchingService;

    @KafkaListener(
            topics = "notice-activated",
            groupId = "pet-notice-group"
    )
    public void consumeNewNotice(NoticeCreatedEvent event) {
        log.info("Odebrano w czasie rzeczywistym nowe ogłoszenie z notice-service! ID: {}, Gatunek: {}, Typ: {}, Opis: {}",
                event.noticeId(), event.species(), event.type(), event.description());
        try {
            matchingService.handleIncomingNotice(event);
        } catch (Exception e) {
            log.error("Błąd podczas przetwarzania wiadomości z notice-service dla ogłoszenia {}", event.noticeId(), e);
        }
    }
}