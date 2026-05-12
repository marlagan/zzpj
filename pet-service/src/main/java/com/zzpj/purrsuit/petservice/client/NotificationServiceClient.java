package com.zzpj.purrsuit.petservice.client;

import com.zzpj.purrsuit.petservice.model.MatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationServiceClient {
    private final WebClient.Builder webClientBuilder;

    public void sendMatchNotification(MatchResult result) {
        try {
            var body = Map.of(
                    "lostNoticeId", result.getLostNoticeId(),
                    "seenNoticeId", result.getSeenNoticeId(),
                    "similarityScore", result.getSimilarityScore()
            );

            webClientBuilder.build()
                    .post()
                    .uri("http://notification-service/api/notifications/send")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnError(e -> log.warn("Failed to notify notification-service: {}", e.getMessage()))
                    .onErrorComplete()
                    .block();
        }catch (Exception e) {
            log.warn("NotificationService unavailable, skipping notification for match: {}",
                    result.getId());
        }
    }
}
