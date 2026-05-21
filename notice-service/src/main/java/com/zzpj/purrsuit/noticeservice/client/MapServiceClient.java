package com.zzpj.purrsuit.noticeservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

/**
 * Klient do map-service.
 * Po aktywacji ogłoszenia (confirm-description lub sighting) rejestruje
 * lokalizację w map-service, żeby ST_DWithin zapytania działały.
 *
 * Używa WebClient z load balancerem (tak jak NotificationServiceClient
 * w pet-service) — spójne z resztą projektu.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MapServiceClient {

    private final WebClient.Builder webClientBuilder;

    public void saveLocation(UUID noticeId, String noticeType,
                             double lat, double lon, Double accuracyRadius) {
        var body = new SaveLocationPayload(noticeId, noticeType, lat, lon, accuracyRadius);
        try {
            webClientBuilder.build()
                    .post()
                    .uri("http://map-service/api/maps/locations")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnError(e -> log.warn("map-service unavailable, skipping location save: {}", e.getMessage()))
                    .onErrorComplete()
                    .block();
        } catch (Exception e) {
            log.warn("map-service call failed for noticeId={}: {}", noticeId, e.getMessage());
        }
    }

    record SaveLocationPayload(
            UUID noticeId,
            String noticeType,
            double lat,
            double lon,
            Double accuracyRadiusMeters
    ) {}
}
