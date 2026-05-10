package com.zzpj.purrsuit.mapservice.controller;

import com.zzpj.purrsuit.mapservice.domain.Failure;
import com.zzpj.purrsuit.mapservice.domain.Success;
import com.zzpj.purrsuit.mapservice.entity.GeoLocation;
import com.zzpj.purrsuit.mapservice.service.LocationMatchingService;
import com.zzpj.purrsuit.mapservice.service.OpenStreetMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maps/locations")
public class MapController {
    private final LocationMatchingService locationService;
    private final OpenStreetMapService mapClient;

    public MapController(
            OpenStreetMapService mapClient,
            LocationMatchingService locationService
            ) {
        this.mapClient = mapClient;
        this.locationService = locationService;
    }

    @Operation(summary = "Pobierz koordynaty z opisu (z użyciem AI)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/analyze-location")
    public ResponseEntity<?> getGeocode(@RequestBody String address) {
        var result = mapClient.geocodeAddress(address);

        // Pattern Matching dla switch (JDK 21)
        return switch (result) {
            case Success s -> ResponseEntity.ok(s.location());
            case Failure f -> ResponseEntity.badRequest().body("Błąd: " + f.reason());
        };
    }

    @PostMapping
    public ResponseEntity<GeoLocation> saveLocation(@RequestBody Map<String, Object> payload) {
        Long petId = payload.containsKey("petId") ? Long.valueOf(payload.get("petId").toString()) : null;
        Long sightingId = payload.containsKey("sightingId") ? Long.valueOf(payload.get("sightingId").toString()) : null;
        double lat = Double.parseDouble(payload.get("lat").toString());
        double lon = Double.parseDouble(payload.get("lon").toString());

        return ResponseEntity.ok(locationService.save(petId, sightingId, lat, lon, null));
    }

    @GetMapping("/near")
    public ResponseEntity<List<GeoLocation>> getLocationsNear(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radiusKm) {

        return ResponseEntity.ok(locationService.getLocationsNear(lat, lng, radiusKm));
    }

    @GetMapping("/matches/{sightingId}")
    public ResponseEntity<List<GeoLocation>> getMatches(
            @PathVariable Long sightingId,
            @RequestParam String species,
            @RequestParam int daysMissing) {

        // Uwaga: w przyszłości species i daysMissing powinien raczej pobierać FeignClient z NoticeService!
        return ResponseEntity.ok(locationService.findMatchesForSighting(sightingId, species, daysMissing));
    }
}