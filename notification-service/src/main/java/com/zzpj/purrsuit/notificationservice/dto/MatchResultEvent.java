package com.zzpj.purrsuit.notificationservice.dto;

import java.util.UUID;

public record MatchResultEvent(
        UUID lostNoticeId,
        UUID seenNoticeId,
        UUID lostOwnerId,
        double similarityScore
) {}