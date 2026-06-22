package com.zzpj.purrsuit.notificationservice.dto;

import java.util.UUID;

public record UserProfileEvent(
        UUID id,
        String email,
        String firstName,
        String lastName
) {}