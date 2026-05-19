package com.zzpj.purrsuit.mapservice.service;

import com.zzpj.purrsuit.mapservice.entity.GeoLocation;
import com.zzpj.purrsuit.mapservice.repository.GeoLocationRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationMatchingService {
    private final GeoLocationRepository repository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public GeoLocation save(UUID noticeId, String noticeType, double lat, double lon, Double accuracyRadiusMeters) {
        // Wykorzystujemy wygodny wzorzec Builder, który dodaliśmy do encji GeoLocation
        GeoLocation location = GeoLocation.builder()
                .noticeId(noticeId)
                .noticeType(noticeType)
                // Uwaga: konstruktor Coordinate przyjmuje kolejność (X, Y), czyli (długość/lon, szerokość/lat)
                .location(geometryFactory.createPoint(new Coordinate(lon, lat)))
                .accuracyRadiusMeters(accuracyRadiusMeters)
                .build();

        // Data createdAt ustawi się automatycznie dzięki adnotacji @PrePersist w encji
        return repository.save(location);
    }

    public double calculateSearchRadius(String species, int daysMissing) {
        double baseRadiusKm = "CAT".equalsIgnoreCase(species) ? 1.0 : 5.0;
        return baseRadiusKm * daysMissing * 1000.0;
    }

    public List<GeoLocation> findMatchesForNotice(UUID noticeId, String species, int daysMissing) {
        GeoLocation originNotice = repository.findByNoticeId(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("Notice location not found in database"));;

        double radiusInMeters = calculateSearchRadius(species, daysMissing);

        return repository.findWithinRadius(
                originNotice.getLocation().getY(),
                originNotice.getLocation().getX(),
                radiusInMeters
        );
    }

    public List<GeoLocation> getLocationsNear(double lat, double lon, double radiusKm) {
        return repository.findWithinRadius(lat, lon, radiusKm * 1000.0);
    }
}
