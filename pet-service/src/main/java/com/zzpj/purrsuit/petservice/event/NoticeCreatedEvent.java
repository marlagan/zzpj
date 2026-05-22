package com.zzpj.purrsuit.petservice.event;

import java.util.UUID;

public record NoticeCreatedEvent(UUID noticeId,
                                 String species,
                                 String description,
                                 double latitude,
                                 double longitude,
                                 String type) {}
