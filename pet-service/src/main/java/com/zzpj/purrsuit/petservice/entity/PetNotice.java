package com.zzpj.purrsuit.petservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

/**
 * Lokalna reprezentacja ogłoszenia
 * Dane te są replikowane z głównego serwisu ogłoszeń poprzez Kafkę i służą
 * do szybkich zapytań i analizy semantycznej opisów przez LLM.
 */

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetNotice {
    @Id
    private UUID noticeId;

    private UUID userId;
    private String type;
    private String species;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String status;
}
