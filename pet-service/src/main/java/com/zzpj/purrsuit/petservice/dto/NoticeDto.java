package com.zzpj.purrsuit.petservice.dto;

import java.util.UUID;

public record NoticeDto(
        UUID id,
        String species,
        String description,
        double latitude,
        double longitude,
        String type // lost or seen
) {}
