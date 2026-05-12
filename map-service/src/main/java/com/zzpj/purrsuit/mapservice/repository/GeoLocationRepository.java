package com.zzpj.purrsuit.mapservice.repository;

import com.zzpj.purrsuit.mapservice.entity.GeoLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeoLocationRepository extends JpaRepository<GeoLocation, Long> {

    @Query(value = "SELECT * FROM geo_locations g WHERE ST_DWithin(g.location\\:\\:geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)\\:\\:geography, :radius)", nativeQuery = true)
    List<GeoLocation> findWithinRadius(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radius
    );

    GeoLocation findBySightingId(Long sightingId);
}
