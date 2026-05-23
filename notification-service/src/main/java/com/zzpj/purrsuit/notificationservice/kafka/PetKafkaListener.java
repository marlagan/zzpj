package com.zzpj.purrsuit.notificationservice.kafka;

import com.zzpj.purrsuit.notificationservice.dto.NotificationDTO;
import com.zzpj.purrsuit.notificationservice.service.EmailService;
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

    @KafkaListener(topics = "pet-match-results", groupId = "purrsuit-group")
    public void consumeMatchResult(String message) {
        log.info("Odebrano event z pet-service: {}", message);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("ownerName", "Mieszko I"); // todo od user-service?
            variables.put("petName", "Puszek"); // todo od user-service?
            // todo variables.put("matchScore", "wysoki"); chcemy??? od pet-service?
            // todo variables.put("distance", ""); chcemy??? od map/pet-service?

            emailService.sendTemplatedEmail(
                    "test@test.com", // todo ściąganie maila od user-service?
                    "Znaleźliśmy potencjalne dopasowanie!",
                    "match-found",
                    variables
            );

            log.info("Email powiadomienie wysłane dla eventu: {}", message);
        } catch (Exception e) {
            log.error("Błąd podczas przetwarzania eventu: {}", message, e);
        }
    }
}