package com.zzpj.purrsuit.common.events;

import java.util.UUID;

public record UserProfileEvent(
        UUID userId,
        String email,
        String firstName,
        String lastName
) {}
