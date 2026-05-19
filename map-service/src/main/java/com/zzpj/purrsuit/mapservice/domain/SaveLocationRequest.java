package com.zzpj.purrsuit.mapservice.domain;

import java.util.UUID;

public record SaveLocationRequest(
        UUID noticeId,             // Identyfikator ogłoszenia (powiązanie z NoticeService)
        String noticeType,         // Typ ogłoszenia: "LOST" lub "FOUND"
        double lat,                // Szerokość geograficzna (Y)
        double lon,                // Długość geograficzna (X)
        Double accuracyRadiusMeters // Promień dokładności (np. 50.0 dla "widziano w okolicy")
) {}