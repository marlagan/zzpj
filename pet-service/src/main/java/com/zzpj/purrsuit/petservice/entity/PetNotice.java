package com.zzpj.purrsuit.petservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;


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
    private String type;    // np. "LOST" lub "FOUND"
    private String species; // np. "kot"

    @Column(columnDefinition = "TEXT")
    private String description;
}
