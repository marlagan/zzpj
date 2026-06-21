package com.zzpj.purrsuit.petservice.entity;

import com.zzpj.purrsuit.petservice.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Encja reprezentująca wynik dopasowania dwóch ogłoszeń.
 * Przechowuje ocenę podobieństwa wyliczoną przez model AI oraz obecny status dopasowania.
 */

@Entity
@Table(name = "match_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID lostNoticeId;

    @Column(nullable = false)
    private UUID seenNoticeId;

    @Column(nullable = false)
    private UUID lostOwnerId;

    /**
     * Współczynnik prawdopodobieństwa (0.0 - 1.0) wyliczony przez LLM.
     */
    @Column(nullable = false)
    private double similarityScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    private LocalDateTime createdAt;

    /**
     * Automatycznie ustawia datę utworzenia rekordu przed zapisem do bazy danych.
     */
    @PrePersist
    void prePersist(){
        this.createdAt = LocalDateTime.now();
    }
}
