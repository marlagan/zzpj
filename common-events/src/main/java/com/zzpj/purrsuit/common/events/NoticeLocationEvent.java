package com.zzpj.purrsuit.common.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record NoticeLocationEvent(
    UUID noticeId,
    String noticeType,
    String species,
    double latitude,
    double longitude,
    LocalDateTime createdAt,
    NoticeStatus noticeStatus
) {}