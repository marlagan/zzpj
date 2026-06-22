package com.zzpj.purrsuit.notificationservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzpj.purrsuit.notificationservice.dto.UserProfileEvent;
import com.zzpj.purrsuit.notificationservice.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileKafkaListener {

    private final UserProfileService userProfileService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-profile-events", groupId = "purrsuit-group")
    public void consumeUserProfile(String message) {
        log.info("Odebrano profil użytkownika z user-service: {}", message);

        try {
            UserProfileEvent event = objectMapper.readValue(message, UserProfileEvent.class);
            userProfileService.save(event);
            log.info("Zapisano profil w bazie: userId={} email={}", event.id(), event.email());
        } catch (Exception e) {
            log.error("Błąd podczas przetwarzania profilu użytkownika: {}", message, e);
        }
    }
}