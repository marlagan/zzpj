package com.zzpj.purrsuit.petservice.event;

import com.zzpj.purrsuit.petservice.dto.NoticeDto;

import java.util.List;
import java.util.UUID;

public record MapMatchEvent(UUID lostNoticeId, List<NoticeDto> foundNotices) {
}
