package com.zzpj.purrsuit.petservice.dto;

import com.zzpj.purrsuit.petservice.enums.MatchStatus;
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
