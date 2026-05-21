package com.zzpj.purrsuit.noticeservice.controller;

import com.zzpj.purrsuit.noticeservice.domain.NoticeStatus;
import com.zzpj.purrsuit.noticeservice.domain.NoticeType;
import com.zzpj.purrsuit.noticeservice.dto.NoticeDto;
import com.zzpj.purrsuit.noticeservice.dto.NoticeDto.*;
import com.zzpj.purrsuit.noticeservice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     */
    @PostMapping
    @Operation(summary = "Utwórz zgłoszenie LOST/FOUND")
    public ResponseEntity<NoticeResponse> createNotice(
            @Valid @RequestBody CreateNoticeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noticeService.createNotice(request));
    }

    /**
     * Użytkownik zatwierdza lub poprawia opis wygenerowany przez AI.
     * Po tej operacji status → ACTIVE, Kafka event → pet-service.
     */
    @PostMapping("/{id}/confirm-description")
    @Operation(summary = "Potwierdź / popraw opis AI — aktywuje zgłoszenie")
    public ResponseEntity<NoticeResponse> confirmAiDescription(
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmAiDescriptionRequest request) {
        return ResponseEntity.ok(noticeService.confirmAiDescription(id, request));
    }

    /**
     * Ktoś widział zwierzę — obserwacja powiązana z istniejącym LOST/FOUND.
     * Wchodzi od razu jako ACTIVE, wysyła Kafka "sighting-created".
     */
    @PostMapping("/sightings")
    @Operation(summary = "Zgłoś obserwację zwierzęcia (SIGHTING)")
    public ResponseEntity<NoticeResponse> createSighting(
            @Valid @RequestBody CreateSightingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noticeService.createSighting(request));
    }

    /**
     * Pobiera zgłoszenie po ID — używane przez pet-service (NoticeServiceClient).
     * Zwraca NoticeResponse; pet-service mapuje sam przez swój NoticeDto record.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Pobierz zgłoszenie po ID")
    public ResponseEntity<NoticeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(noticeService.getById(id));
    }

    /**
     * Filtrowanie po type i status.
     * Pet-service woła: GET /api/notices?type=LOST&status=ACTIVE
     * (stub używał ?type=LOST&confirmed=true → mapujemy confirmed=true jako status=ACTIVE)
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
     * Lista obserwacji (SIGHTING) dla danego zgłoszenia.
     * Front może pokazać "inni widzieli tego kota w tych miejscach".
     */
    @GetMapping("/{id}/sightings")
    @Operation(summary = "Obserwacje powiązane ze zgłoszeniem")
    public ResponseEntity<List<NoticeResponse>> getSightings(@PathVariable UUID id) {
        return ResponseEntity.ok(noticeService.getSightingsForNotice(id));
    }

    /**
     * Zmiana statusu — używana przez pet-service gdy znajdzie match
     * (PENDING_MATCH) lub przez użytkownika (RESOLVED, CLOSED).
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Zmień status zgłoszenia")
    public ResponseEntity<NoticeResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam NoticeStatus status) {
        return ResponseEntity.ok(noticeService.updateStatus(id, status));
    }
}
