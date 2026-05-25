package com.zzpj.purrsuit.petservice.event;


import java.util.List;
import java.util.UUID;

public record MapMatchEvent(UUID lostNoticeId, List<UUID> foundNotices) {
}
