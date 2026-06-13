package com.zzpj.purrsuit.common.events;

import java.util.UUID;

public record NoticeCreatedEvent(UUID noticeId,
                                 String species,
                                 String description,
                                 String type) {}
