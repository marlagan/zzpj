package com.zzpj.purrsuit.mapservice.service;

import com.zzpj.purrsuit.mapservice.domain.GeocodeResponse;
import com.zzpj.purrsuit.mapservice.domain.GeocodingResult;
import com.zzpj.purrsuit.mapservice.domain.LocationDto;
import com.zzpj.purrsuit.mapservice.domain.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OpenStreetMapService {

    private final RestClient restClient;

    public OpenStreetMapService(
            RestClient.Builder builder,
            @Value("${osm.email}") String osmEmail
    ) {
        this.restClient = builder.baseUrl("https://nominatim.openstreetmap.org")
                // Dynamicznie doklejamy wstrzyknięty e-mail do nagłówka
                .defaultHeader("User-Agent", "Purrsuit-App/1.0 (" + osmEmail + ")")
                .build();
    }

    public GeocodingResult geocodeAddress(String address) {
        try {
            // Local-Variable Type Inference (var)
            var response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", address)
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(GeocodeResponse[].class);

            if (response != null && response.length > 0) {
                var firstHit = response[0];
                var location = new LocationDto(
                        firstHit.display_name(),
                        Double.parseDouble(firstHit.lat()),
                        Double.parseDouble(firstHit.lon())
                );
                return new Success(location);
            }
            return new Failure("Address not found");
        } catch (Exception e) {
            return new Failure(e.getMessage());
        }
    }
}