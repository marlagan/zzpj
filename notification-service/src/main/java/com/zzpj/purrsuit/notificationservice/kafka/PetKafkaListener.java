package com.zzpj.purrsuit.notificationservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzpj.purrsuit.notificationservice.dto.MatchResultEvent;
import com.zzpj.purrsuit.notificationservice.entity.Notification;
import com.zzpj.purrsuit.notificationservice.enums.NotificationChannel;
import com.zzpj.purrsuit.notificationservice.enums.NotificationType;
import com.zzpj.purrsuit.notificationservice.service.EmailService;
import com.zzpj.purrsuit.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetKafkaListener {

    private final EmailService emailService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "pet-match-results", groupId = "purrsuit-group")
    public void consumeMatchResult(String message) {
        log.info("Odebrano event z pet-service: {}", message);

        try {
            MatchResultEvent event = objectMapper.readValue(message, MatchResultEvent.class);
            // in-app do bazy
            Notification notification = Notification.builder()
                    .userId(event.lostNoticeId()) // todo: tu ma być userId właściciela z UserService
                    .title("Znaleźliśmy potencjalne dopasowanie!")
                    .message("Znaleziono zwierzę podobne do Twojego. Podobieństwo: "
                            + Math.round(event.similarityScore() * 100) + "%")
                    .type(NotificationType.MATCH_FOUND)
                    .channel(NotificationChannel.IN_APP)
                    .build();
            notificationService.save(notification);

            Map<String, Object> variables = new HashMap<>();
            variables.put("ownerName", "Właścicielu"); // todo: od user-service?
            variables.put("petName", "Twój kotek"); // todo: od user-service?
            variables.put("matchScore", Math.round(event.similarityScore() * 100));
            //variables.put("distance", ""); chcemy??? od map/pet-service?

            emailService.sendTemplatedEmail(
                    "test@test.com", // todo: ściąganie maila od user-service?
                    "Znaleźliśmy potencjalne dopasowanie!",
                    "match-found",
                    variables
            );

            log.info("Powiadomienie zapisane do bazy i email wysłany wysłane dla eventu: {}", message);
        } catch (Exception e) {
            log.error("Błąd podczas przetwarzania eventu: {}", message, e);
        }
    }
}