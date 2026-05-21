package com.zzpj.purrsuit.noticeservice.entity;

import com.zzpj.purrsuit.noticeservice.domain.NoticeStatus;
import com.zzpj.purrsuit.noticeservice.domain.NoticeType;
import lombok.*;
import org.locationtech.jts.geom.Point;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoticeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoticeStatus status;

    @Column(nullable = false)
    private UUID reportedByUserId;

    @Column(nullable = false)
    private String species;

    private String breed;

    @Column(length = 500)
    private String colorDescription;

    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

//    private String photoUrl;

    /**
     * Opis wygenerowany przez AI.
     * Wyświetlany użytkownikowi do weryfikacji przed aktywacją ogłoszenia.
     * Pole "description" udostępniane pet-service pochodzi właśnie stąd
     */
    @Column(columnDefinition = "TEXT")
    private String aiGeneratedDescription;

    /**
     * Czy użytkownik zatwierdził opis AI.
     * Dopóki false — status = PENDING_AI_REVIEW, ogłoszenie niewidoczne.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean aiDescriptionConfirmed = false;

    /** Wypełnione tylko dla type=SIGHTING. */
    private UUID parentNoticeId;

    @Column(columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = NoticeStatus.PENDING_AI_REVIEW;
        }
    }
}
