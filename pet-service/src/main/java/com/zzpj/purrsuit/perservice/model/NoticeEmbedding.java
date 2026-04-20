package com.zzpj.purrsuit.perservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "notice_embeddings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NoticeEmbedding {

    @Id
    private UUID noticeId;

    @Column(nullable = false, columnDefinition = "float8[]") // potem można podmienić na pgvector
    private double[] embedding;

}
