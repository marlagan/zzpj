package com.zzpj.purrsuit.noticeservice.kafka;

import com.zzpj.purrsuit.noticeservice.domain.NoticeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Zdarzenie publikowane na topic "notice-activated" po tym,
 * jak użytkownik potwierdzi opis AI i zgłoszenie stanie się ACTIVE.
 *
 * Konsumuje je matching-service — szuka pasujących zgłoszeń
 * w zadanym promieniu geograficznym.
 */
@Data
@Builder
public class NoticeActivatedEvent {

    private UUID noticeId;
    private NoticeType type;
    private UUID reportedByUserId;

    private String species;
    private String breed;
    private String aiGeneratedDescription;

    private Double latitude;
    private Double longitude;

    private LocalDateTime eventDate;
    private LocalDateTime activatedAt;
}
