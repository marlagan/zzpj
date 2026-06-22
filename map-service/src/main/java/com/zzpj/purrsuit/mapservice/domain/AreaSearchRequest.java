package com.zzpj.purrsuit.mapservice.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Żądanie wyszukiwania ogłoszeń wewnątrz zdefiniowanego wielokąta (obszaru)")
public record AreaSearchRequest(
        @Schema(description = "Lista punktów tworzących wielokąt (min. 3 punkty)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<PointDto> polygonPoints,

        @Schema(description = "Filtr gatunku zwierzęcia",
                example = "DOG", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String species,
        @Schema(description = "Typ ogłoszenia źródłowego, " +
                "który ma zostać WYLUCZONY (szukamy ogłoszeń przeciwstawnych, np. dla LOST szukamy FOUND)",
                example = "LOST")
        String originType
) {}