package com.zzpj.purrsuit.mapservice.service;

import com.zzpj.purrsuit.common.events.NearbyNoticesEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


/**
 * Producent komunikatów Kafka w warstwie map-service.
 * Odpowiada za asynchroniczne powiadamianie innych mikroserwisów o znalezieniu
 * przestrzennych dopasowań pomiędzy zgubionymi i znalezionymi zwierzętami.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationMatchKafkaProducer {

    private final KafkaTemplate<String, NearbyNoticesEvent> kafkaTemplate;
    private static final String TOPIC = "nearby-notices-topic";

    /**
     * Emituje zdarzenie do brokera Kafka o znalezieniu powiązanych ogłoszeń.
     * Inne moduły (np. notification-service) mogą zasubskrybować ten temat,
     * aby powiadomić użytkowników o potencjalnym dopasowaniu ich zwierzaka.
     *
     * @param lostNoticeId  Identyfikator głównego ogłoszenia (tego, dla którego szukano dopasowań).
     * @param nearbyFoundNoticeIds Lista identyfikatorów ogłoszeń znalezionych w wyliczonym promieniu.
     */
    public void sendNearbyNotices(UUID lostNoticeId, List<UUID> nearbyFoundNoticeIds) {
        if (nearbyFoundNoticeIds.isEmpty()) {
            log.info("Brak znalezionych zwierzaków w okolicy dla zgłoszenia {}", lostNoticeId);
            return;
        }

        NearbyNoticesEvent event = new NearbyNoticesEvent(lostNoticeId, nearbyFoundNoticeIds);

        // Jako klucza (drugi argument) używamy lostNoticeId, aby zagwarantować
        // kolejność przetwarzania dla konkretnego zgłoszenia w tej samej partycji
        kafkaTemplate.send(TOPIC, lostNoticeId.toString(), event);

        log.info("Wysłano event o zwierzakach w okolicy dla zgłoszenia: {}", lostNoticeId);
    }
}