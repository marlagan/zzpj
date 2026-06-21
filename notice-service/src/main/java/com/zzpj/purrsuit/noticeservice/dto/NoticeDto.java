package com.zzpj.purrsuit.noticeservice.dto;

import com.zzpj.purrsuit.common.events.NoticeStatus;
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
        @NotNull public NoticeType type;           // LOST lub FOUND
        @NotNull public String species;
        public String breed;
        public String colorDescription;
        public String additionalNotes;
        public String photoUrl;
        @NotNull public Double latitude;
        @NotNull public Double longitude;
        @NotNull public LocalDateTime eventDate;
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
        public String photoUrl;
        public String aiGeneratedDescription;
        public Double latitude;
        public Double longitude;
        public LocalDateTime eventDate;
        public LocalDateTime createdAt;
    }

    /**
     * Odpowiada dokładnie rekordowi NoticeDto w pet-service:
     *   record NoticeDto(UUID id, String species, String description,
     *                    double latitude, double longitude, String type)
     *
     * Pole description = aiGeneratedDescription ?? colorDescription ?? species
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
