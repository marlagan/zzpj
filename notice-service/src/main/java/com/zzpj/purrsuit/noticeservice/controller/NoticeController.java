package com.zzpj.purrsuit.noticeservice.controller;

import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.noticeservice.domain.NoticeType;
import com.zzpj.purrsuit.noticeservice.dto.NoticeDto.*;
import com.zzpj.purrsuit.noticeservice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Tag(name = "Notice", description = "Zarządzanie zgłoszeniami zgubionych / znalezionych zwierząt")
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * Tworzy zgłoszenie LOST lub FOUND.
     * Groq automatycznie generuje opis z danych formularza.
     * Ogłoszenie wchodzi od razu jako ACTIVE.
     *
     * UUID zgłaszającego NIE jest przyjmowany z body requestu — pobierany jest
     * z tokenu JWT zalogowanego użytkownika (claim "sub"), dzięki czemu nie da się
     * utworzyć zgłoszenia w imieniu innego użytkownika. To samo UUID trafia
     * następnie do pet-service przez event Kafka "notice-activated".
     */
    @PostMapping
    @Operation(summary = "Utwórz zgłoszenie LOST lub FOUND")
    public ResponseEntity<NoticeResponse> createNotice(
            @Valid @RequestBody CreateNoticeRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID reportedByUserId = UUID.fromString(jwt.getClaimAsString("sub"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noticeService.createNotice(request, reportedByUserId));
    }

    /**
     * Pobiera zgłoszenie po ID.
     * Używane przez pet-service (NoticeServiceClient).
     */
    @GetMapping("/{id}")
    @Operation(summary = "Pobierz zgłoszenie po ID")
    public ResponseEntity<NoticeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(noticeService.getById(id));
    }

    /**
     * Lista zgłoszeń z filtrem po type i status.
     * Pet-service woła: GET /api/notices?type=LOST&status=ACTIVE
     */
    @GetMapping
    @Operation(summary = "Lista zgłoszeń — filtr po type i status")
    public ResponseEntity<List<NoticeResponse>> getByTypeAndStatus(
            @RequestParam NoticeType type,
            @RequestParam(defaultValue = "ACTIVE") NoticeStatus status) {
        return ResponseEntity.ok(noticeService.getByTypeAndStatus(type, status));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Zgłoszenia danego użytkownika")
    public ResponseEntity<List<NoticeResponse>> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(noticeService.getByUser(userId));
    }

    /**
     * Zmiana statusu — wywoływana przez pet-service (PENDING_MATCH)
     * lub przez użytkownika (RESOLVED, CLOSED).
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Zmień status zgłoszenia")
    public ResponseEntity<NoticeResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam NoticeStatus status) {
        return ResponseEntity.ok(noticeService.updateStatus(id, status));
    }
}
