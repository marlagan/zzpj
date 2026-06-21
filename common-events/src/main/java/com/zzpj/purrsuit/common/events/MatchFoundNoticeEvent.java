package com.zzpj.purrsuit.common.events;

import java.util.UUID;

public record MatchFoundNoticeEvent(
        UUID lostNoticeId,
        UUID seenNoticeId,
        double similarityScore
) {
}
