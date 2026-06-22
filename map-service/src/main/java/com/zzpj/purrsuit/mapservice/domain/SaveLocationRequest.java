package com.zzpj.purrsuit.mapservice.domain;

import com.zzpj.purrsuit.common.events.NoticeStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Model danych do utworzenia nowego punktu na mapie")
public record SaveLocationRequest(
        @Schema(description = "Unikalny identyfikator powiązanego ogłoszenia",
            example = "123e4567-e89b-12d3-a456-426614174000")
        UUID noticeId,             // Identyfikator ogłoszenia (powiązanie z NoticeService)

        @Schema(description = "Typ ogłoszenia (LOST / FOUND)", example = "LOST")
        String noticeType,         // Typ ogłoszenia: "LOST" lub "FOUND"

        @Schema(description = "Gatunek zwierzęcia", example = "CAT")
        String species,

        @Schema(description = "Szerokość geograficzna (Y)", example = "51.759248")
        double lat,                // Szerokość geograficzna (Y)

        @Schema(description = "Długość geograficzna (X)", example = "19.455983")
        double lon,                // Długość geograficzna (X)

        @Schema(description = "Promień dokładności w metrach (np. z GPS urządzenia)", example = "15.5")
        Double accuracyRadiusMeters, // Promień dokładności (np. 50.0 dla "widziano w okolicy")

        @Schema(description = "Aktualny status ogłoszenia", example = "ACTIVE")
        NoticeStatus noticeStatus
) {}