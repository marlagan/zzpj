package com.zzpj.purrsuit.noticeservice.entity;

import com.zzpj.purrsuit.common.events.NoticeStatus;
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
    private NoticeType type; // LOST | FOUND

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

    private String photoUrl;

    /**
     * Opis wygenerowany przez Groq na podstawie danych z formularza.
     * Pokazywany na liście ogłoszeń — pomaga innej osobie rozpoznać zwierzę.
     */
    @Column(columnDefinition = "TEXT")
    private String aiGeneratedDescription;

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
            this.status = NoticeStatus.ACTIVE;
        }
    }
}
