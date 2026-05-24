package com.zzpj.purrsuit.mapservice.dto; // lub mapservice.dto w drugim serwisie

import java.time.LocalDateTime;
import java.util.UUID;

public record NoticeLocationEvent(
    UUID noticeId,
    String noticeType,
    double latitude,
    double longitude,
    LocalDateTime createdAt
) {}