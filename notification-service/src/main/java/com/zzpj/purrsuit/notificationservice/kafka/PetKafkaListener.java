package com.zzpj.purrsuit.notificationservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzpj.purrsuit.notificationservice.dto.MatchResultEvent;
import com.zzpj.purrsuit.notificationservice.dto.PetNotificationDTO;
import com.zzpj.purrsuit.notificationservice.entity.Notification;
import com.zzpj.purrsuit.notificationservice.entity.UserProfile;
import com.zzpj.purrsuit.notificationservice.enums.NotificationChannel;
import com.zzpj.purrsuit.notificationservice.enums.NotificationType;
import com.zzpj.purrsuit.notificationservice.service.*;
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
    private final UserProfileService userProfileService;
    private final PetServiceClient petServiceClient;

    @KafkaListener(topics = "pet-match-results", groupId = "purrsuit-group")
    public void consumeMatchResult(String message) {
        log.info("Odebrano event z pet-service: {}", message);

        try {
            MatchResultEvent event = objectMapper.readValue(message, MatchResultEvent.class);

            UserProfile profile = userProfileService.get(event.lostOwnerId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Brak profilu użytkownika w bazie dla userId=" + event.lostOwnerId()
                                    + " — profil nie został jeszcze zsynchronizowany"));

            PetNotificationDTO petNotificationDTO = petServiceClient.getNotification(event.lostNoticeId());

            Notification notification = Notification.builder()
                    .userId(event.lostOwnerId())
                    .title("Znaleźliśmy potencjalne dopasowanie!")
                    .message("Znaleziono zwierzę podobne do Twojego. Podobieństwo: "
                            + Math.round(event.similarityScore() * 100) + "%")
                    .type(NotificationType.MATCH_FOUND)
                    .channel(NotificationChannel.IN_APP)
                    .build();
            notificationService.save(notification);

            Map<String, Object> variables = new HashMap<>();
            variables.put("ownerName", profile.getFirstName());
            variables.put("petName", petNotificationDTO.getSpecies());

            emailService.sendTemplatedEmail(
                    profile.getEmail(),
                    "Znaleźliśmy potencjalne dopasowanie!",
                    "match-found",
                    variables
            );

            log.info("Powiadomienie zapisane do bazy i email wysłany dla userId={}", event.lostOwnerId());
        } catch (Exception e) {
            log.error("Błąd podczas przetwarzania eventu: {}", message, e);
        }
    }
}