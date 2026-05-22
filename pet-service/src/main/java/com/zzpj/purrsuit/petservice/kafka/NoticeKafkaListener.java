//package com.zzpj.purrsuit.petservice.kafka;
//
//import com.zzpj.purrsuit.petservice.event.NoticeCreatedEvent;
//import com.zzpj.purrsuit.petservice.service.MatchingService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class NoticeKafkaListener { //do uzgodnienia z notice-service
//
//    private final MatchingService matchingService;
//
//    @KafkaListener(
//            topics = "notices-topic",
//            groupId = "pet-notice-group",
//            containerFactory = "noticeKafkaListenerContainerFactory"
//    )
//    public void consumeNewNotice(NoticeCreatedEvent event) {
//        log.info("Odebrano w czasie rzeczywistym nowe ogłoszenie z notice-service! ID: {}, Gatunek: {}, Typ: {}",
//                event.noticeId(), event.species(), event.type());
//        try {
//            // Przekazujemy do serwisu (np. do ewentualnego zapisu lub logowania)
//            matchingService.handleIncomingNotice(event);
//        } catch (Exception e) {
//            log.error("Błąd podczas przetwarzania wiadomości z notice-service dla ogłoszenia {}", event.noticeId(), e);
//        }
//    }
//}