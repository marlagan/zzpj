package com.zzpj.purrsuit.notificationservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzpj.purrsuit.common.events.UserProfileEvent;
import com.zzpj.purrsuit.notificationservice.service.UserProfileCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileKafkaListener {

    private final UserProfileCache userProfileCache;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-profile-events", groupId = "purrsuit-group")
    public void consumeUserProfile(String message) {
        log.info("Odebrano profil użytkownika z user-service: {}", message);

        try {
            UserProfileEvent event = objectMapper.readValue(message, UserProfileEvent.class);
            userProfileCache.put(event);
            log.info("Zapisano profil w cache: userId={} email={}", event.userId(), event.email());
        } catch (Exception e) {
            log.error("Błąd podczas przetwarzania profilu użytkownika: {}", message, e);
        }
    }
}
