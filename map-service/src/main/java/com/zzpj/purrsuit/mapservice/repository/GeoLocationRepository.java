package com.zzpj.purrsuit.mapservice.repository;

import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.mapservice.entity.GeoLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query(value = """
            SELECT * FROM geo_locations g
            WHERE g.species = :species
              AND g.notice_status IN ('ACTIVE', 'PENDING_MATCH')
              AND g.notice_type != :originType
              AND ST_DWithin(g.location\\:\\:geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)\\:\\:geography, :radius)
            """, nativeQuery = true)
    List<GeoLocation> findWithinRadiusAndCriteria(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radius,
            @Param("species") String species,
            @Param("originType") String originType
    );

    @Query(value = """
            SELECT * FROM geo_locations g
            WHERE ST_Within(g.location, ST_GeomFromText(:polygonWkt, 4326))
              AND (:species IS NULL OR g.species = :species)
              AND (:originType IS NULL OR g.notice_type != :originType)
            """, nativeQuery = true)
    List<GeoLocation> findWithinPolygon(
            @Param("polygonWkt") String polygonWkt,
            @Param("species") String species,
            @Param("originType") String originType
    );

    Optional<GeoLocation> findByNoticeId(UUID noticeId);

    @Modifying
    @Query("UPDATE GeoLocation g SET g.noticeStatus = :noticeStatus WHERE g.noticeId = :noticeId")
    void updateStatusByNoticeId(@Param("noticeId") UUID noticeId, @Param("noticeStatus") NoticeStatus noticeStatus);
}
