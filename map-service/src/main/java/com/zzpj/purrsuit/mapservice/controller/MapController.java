package com.zzpj.purrsuit.mapservice.controller;

import com.zzpj.purrsuit.mapservice.domain.Failure;
import com.zzpj.purrsuit.mapservice.domain.Success;
import com.zzpj.purrsuit.mapservice.service.OpenStreetMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maps")
public class MapController {

    private final OpenStreetMapService mapClient;

    public MapController(OpenStreetMapService mapClient) {
        this.mapClient = mapClient;
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
}