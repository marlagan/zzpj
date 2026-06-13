package com.zzpj.purrsuit.common.events;

import java.util.UUID;

public record NoticeCreatedEvent(UUID noticeId,
                                 UUID userId,
                                 String species,
                                 String description,
                                 String type) {}
