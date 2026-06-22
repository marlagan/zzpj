package com.zzpj.purrsuit.noticeservice.controller;

import com.zzpj.purrsuit.noticeservice.dto.NoticeMatchDto;
import com.zzpj.purrsuit.noticeservice.entity.NoticeMatch;
import com.zzpj.purrsuit.noticeservice.service.NoticeMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notices/matches")
@RequiredArgsConstructor
@Tag(name = "Notice Match", description = "Dopasowania zgłoszeń znalezione przez pet-service")
public class NoticeMatchController {

    private final NoticeMatchService noticeMatchService;

    @GetMapping("/notice/{noticeId}")
    @Operation(summary = "Lista dopasowań powiązanych z danym zgłoszeniem")
    public ResponseEntity<List<NoticeMatchDto>> getMatchesForNotice(@PathVariable UUID noticeId) {
        return ResponseEntity.ok(noticeMatchService.getMatchesForNotice(noticeId)
                .stream().map(this::toDto).toList());
    }

    /**
     * Potwierdza odnalezienie zwierzęcia. Oba zgłoszenia przechodzą w RESOLVED,
     * a map-service oraz pet-service są o tym powiadamiane przez Kafkę.
     * Wywołać może wyłącznie jedna ze stron zgłoszenia (LOST lub FOUND) —
     * UUID użytkownika pobierany jest z tokenu JWT.
     */
    @PatchMapping("/{matchId}/confirm")
    @Operation(summary = "Potwierdź dopasowanie / odnalezienie zwierzęcia")
    public ResponseEntity<NoticeMatchDto> confirmMatch(
            @PathVariable UUID matchId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requestingUserId = UUID.fromString(jwt.getClaimAsString("sub"));
        return ResponseEntity.ok(toDto(noticeMatchService.confirmMatch(matchId, requestingUserId)));
    }

    /**
     * Odrzuca dopasowanie. Oba zgłoszenia wracają w status ACTIVE,
     * a map-service oraz pet-service są o tym powiadamiane przez Kafkę.
     */
    @PatchMapping("/{matchId}/reject")
    @Operation(summary = "Odrzuć dopasowanie")
    public ResponseEntity<NoticeMatchDto> rejectMatch(
            @PathVariable UUID matchId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requestingUserId = UUID.fromString(jwt.getClaimAsString("sub"));
        return ResponseEntity.ok(toDto(noticeMatchService.rejectMatch(matchId, requestingUserId)));
    }

    private NoticeMatchDto toDto(NoticeMatch m) {
        return NoticeMatchDto.builder()
                .id(m.getId())
                .lostNoticeId(m.getLostNoticeId())
                .seenNoticeId(m.getSeenNoticeId())
                .lostOwnerId(m.getLostOwnerId())
                .similarityScore(m.getSimilarityScore())
                .status(m.getStatus())
                .createdAt(m.getCreatedAt())
                .decidedAt(m.getDecidedAt())
                .build();
    }
}
