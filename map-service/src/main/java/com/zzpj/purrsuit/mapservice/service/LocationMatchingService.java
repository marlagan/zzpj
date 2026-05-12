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

@Service
@RequiredArgsConstructor
public class LocationMatchingService {
    private final GeoLocationRepository repository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public GeoLocation save(Long petId, Long sightingId, double lat, double lon, Double radius) {
        GeoLocation location = new GeoLocation();
        location.setPetId(petId);
        location.setSightingId(sightingId);
        location.setRadius(radius);
        location.setLocation(geometryFactory.createPoint(new Coordinate(lon, lat)));
        location.setCreatedAt(LocalDateTime.now());
        return repository.save(location);
    }

    public double calculateSearchRadius(String species, int daysMissing) {
        double baseRadiusKm = "CAT".equalsIgnoreCase(species) ? 1.0 : 5.0;
        return baseRadiusKm * daysMissing * 1000.0;
    }

    public List<GeoLocation> findMatchesForSighting(Long sightingId, String species, int daysMissing) {
        GeoLocation sighting = repository.findBySightingId(sightingId);
        if (sighting == null) {
            throw new IllegalArgumentException("Sighting not found in database");
        }

        double radiusInMeters = calculateSearchRadius(species, daysMissing);

        return repository.findWithinRadius(
                sighting.getLocation().getY(),
                sighting.getLocation().getX(),
                radiusInMeters
        );
    }

    public List<GeoLocation> getLocationsNear(double lat, double lon, double radiusKm) {
        return repository.findWithinRadius(lat, lon, radiusKm * 1000.0);
    }
}
