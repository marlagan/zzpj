package com.zzpj.purrsuit.mapservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zzpj.purrsuit.common.events.NoticeStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "geo_locations")
public class GeoLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID noticeId;

    @Column(nullable = false, length = 20)
    private String noticeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoticeStatus noticeStatus;

    private String species;

    // Współrzędne geograficzne — JTS Point nie serializuje się do JSON (StackOverflowError)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point location;

    @JsonIgnore
    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    @JsonProperty("location")
    public Map<String, Object> getLocationForJson() {
        if (location == null) {
            return null;
        }
        return Map.of(
                "type", "Point",
                "coordinates", List.of(location.getX(), location.getY())
        );
    }

    @Column(name = "accuracy_radius_meters")
    private Double accuracyRadiusMeters;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}