package com.zzpj.purrsuit.petservice.controller;

import com.zzpj.purrsuit.petservice.dto.MatchResultDto;
import com.zzpj.purrsuit.petservice.entity.MatchResult;
import com.zzpj.purrsuit.petservice.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name= "Match Results", description = " Api pozwalające uzyskać dostęp do wyników dopasowań zagubionych i znalezionych zwierząt")
public class PetController {
    private final MatchingService matchingService;


    @Operation(
            summary = "Pobierz wszystkie dopasowania dla ogłoszenia",
            description = "Zwraca liste wszystkich możliwych dopasowań dla danego zgłoszenia"
    )
    @GetMapping("matches/{noticeId}")
    public ResponseEntity<List<MatchResultDto>> getMatches(
            @Parameter(description = "Identyfikator UUID ogłoszenia bazowego (np. zgubionego zwierzaka)", required = true)
            @PathVariable UUID noticeId){
        var results = matchingService.getMatchesForNotice(noticeId);
        var dtos = results.stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(
            summary = "Pobierz szczegóły konkretnego dopasowania",
            description = "Zwraca dokładny wynik dopasowania między ogłoszeniem bazowym a konkretnym ogłoszeniem kandydującym."
    )
    @GetMapping("matches/{noticeId}/{candidateId}")
    public ResponseEntity<MatchResultDto> getMatchDetail(
            @Parameter(description = "Identyfikator UUID ogłoszenia bazowego", required = true)
            @PathVariable UUID noticeId,
            @Parameter(description = "Identyfikator UUID ogłoszenia kandydata", required = true)
            @PathVariable UUID candidateId){
        return matchingService.getMatchDetail(noticeId, candidateId)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    private MatchResultDto toDto(MatchResult result) {
        return new MatchResultDto(
                result.getId(),
                result.getLostNoticeId(),
                result.getSeenNoticeId(),
                result.getLostOwnerId(),
                result.getSimilarityScore(),
                result.getStatus(),
                result.getCreatedAt());
    }
}
