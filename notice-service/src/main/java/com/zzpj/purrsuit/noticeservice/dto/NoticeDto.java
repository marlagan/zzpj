package com.zzpj.purrsuit.noticeservice.dto;

import com.zzpj.purrsuit.noticeservice.domain.NoticeStatus;
import com.zzpj.purrsuit.noticeservice.domain.NoticeType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public final class NoticeDto {

    private NoticeDto() {}

    @Data
    public static class CreateNoticeRequest {
        @NotNull public NoticeType type;
        @NotNull public UUID reportedByUserId;
        @NotNull public String species;
        public String breed;
        public String colorDescription;
        public String additionalNotes;
        /** Groq nie analizuje zdjęć, można pomyśleć co z tym */
//        public String photoUrl;
        @NotNull public Double latitude;
        @NotNull public Double longitude;
        @NotNull public LocalDateTime eventDate;
    }

    @Data
    public static class CreateSightingRequest {
        /** ID ogłoszenia LOST/FOUND, do którego nawiązuje obserwacja. */
        @NotNull public UUID parentNoticeId;
        @NotNull public UUID reportedByUserId;
//        public String photoUrl;
        public String additionalNotes;
        @NotNull public Double latitude;
        @NotNull public Double longitude;
        @NotNull public LocalDateTime eventDate;
    }

    @Data
    public static class ConfirmAiDescriptionRequest {
        /**
         * Opis zatwierdzony/poprawiony przez użytkownika.
         * Po potwierdzeniu status → ACTIVE i Kafka event do pet-service.
         */
        @NotNull public String confirmedDescription;
    }

    @Data
    @Builder
    public static class NoticeResponse {
        public UUID id;
        public NoticeType type;
        public NoticeStatus status;
        public UUID reportedByUserId;
        public String species;
        public String breed;
        public String colorDescription;
        public String additionalNotes;
//        public String photoUrl;
        public String aiGeneratedDescription;
        public boolean aiDescriptionConfirmed;
        public UUID parentNoticeId;
        public Double latitude;
        public Double longitude;
        public LocalDateTime eventDate;
        public LocalDateTime createdAt;
    }

    /**
     * DTO odpowiadające dokładnie polu NoticeDto w pet-service:
     *   record NoticeDto(UUID id, String species, String description,
     *                    double latitude, double longitude, String type)
     *
     * Używane przez GET /api/notices/{id} i GET /api/notices?type=&status=
     * odpowiadające metodom NoticeServiceClient w pet-service.
     *
     * Pole "description" = aiGeneratedDescription (jeśli istnieje)
     *                     lub colorDescription (fallback).
     */
    @Data
    @Builder
    public static class PetServiceNoticeDto {
        public UUID id;
        public String species;
        public String description;
        public double latitude;
        public double longitude;
        public String type;
    }
}
