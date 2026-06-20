package com.zzpj.purrsuit.notificationservice.service;

import com.zzpj.purrsuit.notificationservice.dto.UserProfileEvent;
import com.zzpj.purrsuit.notificationservice.entity.UserProfile;
import com.zzpj.purrsuit.notificationservice.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public void save(UserProfileEvent event) {
        UserProfile profile = UserProfile.builder()
                .userId(event.id())
                .email(event.email())
                .firstName(event.firstName())
                .lastName(event.lastName())
                .build();
        userProfileRepository.save(profile);
    }

    public Optional<UserProfile> get(UUID userId) {
        return userProfileRepository.findById(userId);
    }
}