package com.zzpj.purrsuit.petservice.model;

import com.zzpj.purrsuit.petservice.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MatchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID lostNoticeId;

    @Column(nullable = false)
    private UUID seenNoticeId;

    @Column(nullable = false)
    private double similarityScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist(){
        this.createdAt = LocalDateTime.now();
    }
}
