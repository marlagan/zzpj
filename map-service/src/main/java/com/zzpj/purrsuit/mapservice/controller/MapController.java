package com.zzpj.purrsuit.mapservice.controller;

import com.zzpj.purrsuit.mapservice.domain.AreaSearchRequest;
import com.zzpj.purrsuit.mapservice.domain.Failure;
import com.zzpj.purrsuit.mapservice.domain.SaveLocationRequest;
import com.zzpj.purrsuit.mapservice.domain.Success;
import com.zzpj.purrsuit.mapservice.entity.GeoLocation;
import com.zzpj.purrsuit.mapservice.service.LocationMatchingService;
import com.zzpj.purrsuit.mapservice.service.OpenStreetMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/maps/locations")
public class MapController {
    private final LocationMatchingService locationService;
    private final OpenStreetMapService mapClient;

    @Operation(summary = "Pobierz koordynaty z opisu (z użyciem AI)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/analyze-location")
    public ResponseEntity<?> getGeocode(@RequestBody Map<String, String> payload) {
        String address = payload.get("address");

        if (address == null || address.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Błąd: Pole 'address' nie może być puste");
        }

        var result = mapClient.geocodeAddress(address);

        // Pattern Matching dla switch (JDK 21)
        return switch (result) {
            case Success s -> ResponseEntity.ok(s.location());
            case Failure f -> ResponseEntity.badRequest().body("Błąd: " + f.reason());
        };
    }

    @PostMapping
    public ResponseEntity<GeoLocation> saveLocation(@RequestBody SaveLocationRequest request) {
        // Dzięki Rekordowi mamy bezpieczeństwo typów (Type Safety) i czysty kod
        return ResponseEntity.ok(locationService.save(
                request.noticeId(),
                request.noticeType(),
                request.species(),
                request.lat(),
                request.lon(),
                request.accuracyRadiusMeters(),
                request.noticeStatus()
        ));
    }

    @GetMapping("/near")
    public ResponseEntity<List<GeoLocation>> getLocationsNear(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double radiusKm) {

        return ResponseEntity.ok(locationService.getLocationsNear(lat, lon, radiusKm));
    }

    @GetMapping("/matches/{noticeId}")
    public ResponseEntity<List<GeoLocation>> getMatches(
            @PathVariable UUID noticeId,
            @RequestParam String species,
            @RequestParam int daysMissing) {

        return ResponseEntity.ok(locationService.findMatchesForNotice(noticeId, species, daysMissing));
    }

    @PostMapping("/search-by-area")
    public ResponseEntity<List<GeoLocation>> getMatchesWithinArea(@RequestBody AreaSearchRequest request) {
        return ResponseEntity.ok(locationService.findMatchesWithinArea(request));
    }
}