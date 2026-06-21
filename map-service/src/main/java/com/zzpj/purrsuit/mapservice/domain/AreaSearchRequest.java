package com.zzpj.purrsuit.mapservice.domain;

import java.util.List;

public record AreaSearchRequest(
        List<PointDto> polygonPoints,
        String species,     // Opcjonalnie: filtruj po gatunku
        String originType   // Opcjonalnie: filtruj po typie (np. szukaj FOUND dla ogłoszenia LOST)
) {}