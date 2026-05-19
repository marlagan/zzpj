package com.zzpj.purrsuit.mapservice.repository;

import com.zzpj.purrsuit.mapservice.entity.GeoLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GeoLocationRepository extends JpaRepository<GeoLocation, UUID> {

    @Query(value = """
            SELECT * FROM geo_locations g
            WHERE ST_DWithin(g.location\\:\\:geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)\\:\\:geography, :radius)
            """, nativeQuery = true)
    List<GeoLocation> findWithinRadius(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radius
    );

    Optional<GeoLocation> findByNoticeId(UUID noticeId);
}
