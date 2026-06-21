package com.zzpj.purrsuit.common.events;

import java.util.UUID;

public record NoticeStatusUpdateEvent(
        UUID noticeId,
        String newStatus
) {}
