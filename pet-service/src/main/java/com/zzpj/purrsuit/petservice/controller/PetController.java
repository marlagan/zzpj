package com.zzpj.purrsuit.petservice.controller;

import com.zzpj.purrsuit.petservice.dto.MatchResultDto;
import com.zzpj.purrsuit.petservice.model.MatchResult;
import com.zzpj.purrsuit.petservice.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {
    private final MatchingService matchingService;


    @GetMapping("matches/{noticeId}")
    public ResponseEntity<List<MatchResultDto>> getMatches(@PathVariable UUID noticeId){
        var results = matchingService.getMatchesForNotice(noticeId);
        var dtos = results.stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("matches/{noticeId}/{candidateId}")
    public ResponseEntity<MatchResultDto> getMatchDetail(@PathVariable UUID noticeId, @PathVariable UUID candidateId){
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
                result.getSimilarityScore(),
                result.getStatus(),
                result.getCreatedAt());
    }
}
