package com.zzpj.purrsuit.noticeservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NoticeLocationEvent(
        UUID noticeId,
        String noticeType,
        double latitude,
        double longitude,
        LocalDateTime createdAt
) {}