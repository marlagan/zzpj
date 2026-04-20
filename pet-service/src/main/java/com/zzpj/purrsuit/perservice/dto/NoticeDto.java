package com.zzpj.purrsuit.perservice.dto;

import java.util.UUID;

public record NoticeDto(
        UUID id,
        String species,
        String aiDecsription,
        double latitude,
        double longitude,
        String type // lost or seen
) {}
