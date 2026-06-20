package com.zzpj.purrsuit.notificationservice.service;

import com.zzpj.purrsuit.common.events.UserProfileEvent;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserProfileCache {

    private final Map<UUID, UserProfileEvent> profiles = new ConcurrentHashMap<>();

    public void put(UserProfileEvent event) {
        profiles.put(event.userId(), event);
    }

    public Optional<UserProfileEvent> get(UUID userId) {
        return Optional.ofNullable(profiles.get(userId));
    }
}
