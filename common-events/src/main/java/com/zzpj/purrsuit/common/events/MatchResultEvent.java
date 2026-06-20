package com.zzpj.purrsuit.common.events;

import java.util.UUID;

public record MatchResultEvent(
        UUID lostNoticeId,
        UUID seenNoticeId,
        UUID userId,
        double similarityScore
) {}