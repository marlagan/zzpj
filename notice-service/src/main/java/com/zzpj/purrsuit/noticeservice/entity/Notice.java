package com.zzpj.purrsuit.noticeservice.entity;

import com.zzpj.purrsuit.noticeservice.enums.NoticeStatus;
import com.zzpj.purrsuit.noticeservice.enums.NoticeType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long petId; // Może być puste, jeśli to zgłoszenie FOUND anonimowego zwierzaka

    @Enumerated(EnumType.STRING)
    private NoticeType type;

    @Column(columnDefinition = "TEXT")
    private String generatedText;

    @ElementCollection
    private List<String> photoUrls;

    @Enumerated(EnumType.STRING)
    private NoticeStatus status;

    private LocalDateTime createdAt;
}
