package com.zzpj.purrsuit.noticeservice.dto;

import com.zzpj.purrsuit.noticeservice.domain.MatchStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NoticeMatchDto {
    public UUID id;
    public UUID lostNoticeId;
    public UUID seenNoticeId;
    public UUID lostOwnerId;
    public double similarityScore;
    public MatchStatus status;
    public LocalDateTime createdAt;
    public LocalDateTime decidedAt;
}
