package com.zzpj.purrsuit.noticeservice.entity;

import com.zzpj.purrsuit.noticeservice.domain.MatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Przechowuje informację o dopasowaniu znalezionym przez pet-service
 * (event "pet-match-results") oraz jego status w obrębie notice-service.
 *
 * Cykl życia:
 *  1. PENDING   — zapisywane po odebraniu MatchResultEvent z pet-service.
 *  2. CONFIRMED — użytkownik (jedna ze stron zgłoszenia) potwierdza odnalezienie.
 *  3. REJECTED  — użytkownik odrzuca dopasowanie, oba zgłoszenia wracają do ACTIVE.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notice_matches")
public class NoticeMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID lostNoticeId;

    @Column(nullable = false)
    private UUID seenNoticeId;

    /**
     * UUID właściciela zgłoszenia LOST, tak jak przekazany przez pet-service
     * w MatchResultEvent#userId(). Dane pomocnicze — autoryzacja przy
     * potwierdzaniu/odrzucaniu opiera się o realnych reportedByUserId obu
     * zgłoszeń (patrz NoticeMatchService).
     */
    @Column(nullable = false)
    private UUID lostOwnerId;

    @Column(nullable = false)
    private double similarityScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime decidedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = MatchStatus.PENDING;
        }
    }
}
