package com.zzpj.purrsuit.mapservice.service;

import com.zzpj.purrsuit.mapservice.domain.AreaSearchRequest;
import com.zzpj.purrsuit.mapservice.domain.PointDto;
import com.zzpj.purrsuit.mapservice.entity.GeoLocation;
import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.mapservice.repository.GeoLocationRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationMatchingService {
    private final GeoLocationRepository repository;
    private final LocationMatchKafkaProducer kafkaProducer;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public GeoLocation save(UUID noticeId, String noticeType, String species,
                            double lat, double lon, Double accuracyRadiusMeters, NoticeStatus noticeStatus) {
        // Wykorzystujemy wygodny wzorzec Builder, który dodaliśmy do encji GeoLocation
        GeoLocation location = GeoLocation.builder()
                .noticeId(noticeId)
                .noticeType(noticeType)
                .species(species) // Zapisujemy gatunek
                .location(geometryFactory.createPoint(new Coordinate(lon, lat)))
                .accuracyRadiusMeters(accuracyRadiusMeters)
                .noticeStatus(noticeStatus)
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
                .orElseThrow(() -> new IllegalArgumentException("Notice location not found in database"));

        double radiusInMeters = calculateSearchRadius(species, daysMissing);

        List<GeoLocation> nearbyLocations = repository.findWithinRadiusAndCriteria(
                originNotice.getLocation().getY(),
                originNotice.getLocation().getX(),
                radiusInMeters,
                originNotice.getSpecies(),
                originNotice.getNoticeType()
        );

        List<UUID> nearbyNoticeIds = nearbyLocations.stream()
                .map(GeoLocation::getNoticeId)
                .filter(id -> !id.equals(noticeId))
                .collect(Collectors.toList());

        kafkaProducer.sendNearbyNotices(noticeId, nearbyNoticeIds);

        return nearbyLocations;
    }

    public List<GeoLocation> getLocationsNear(double lat, double lon, double radiusKm) {
        return repository.findWithinRadius(lat, lon, radiusKm * 1000.0);
    }

    public List<GeoLocation> findMatchesWithinArea(AreaSearchRequest request) {
        List<PointDto> points = request.polygonPoints();

        if (points == null || points.size() < 3) {
            throw new IllegalArgumentException("Wielokąt obszaru musi składać się z co najmniej 3 punktów.");
        }

        PointDto firstPoint = points.get(0);
        PointDto lastPoint = points.get(points.size() - 1);
        boolean isClosed = firstPoint.lat() == lastPoint.lat() && firstPoint.lon() == lastPoint.lon();

        int coordinateArraySize = points.size() + (isClosed ? 0 : 1);
        Coordinate[] coordinates = new Coordinate[coordinateArraySize];

        for (int i = 0; i < points.size(); i++) {
            coordinates[i] = new Coordinate(points.get(i).lon(), points.get(i).lat());
        }

        if (!isClosed) {
            coordinates[coordinateArraySize - 1] = new Coordinate(firstPoint.lon(), firstPoint.lat());
        }

        Polygon jtsPolygon = geometryFactory.createPolygon(coordinates);

        String wktPolygon = jtsPolygon.toText();

        return repository.findWithinPolygon(
                wktPolygon,
                request.species(),
                request.originType()
        );
    }
}
