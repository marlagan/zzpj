package com.zzpj.purrsuit.perservice.dto;

import com.zzpj.purrsuit.perservice.enums.MatchStatus;
import java.util.UUID;
import java.time.LocalDateTime;

public record MatchResultDto(
        UUID id,
        UUID lostNoticeId,
        UUID seenNoticeId,
        double similarityScore,
        MatchStatus status,
        LocalDateTime createdAt
) {}
