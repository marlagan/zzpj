package com.zzpj.purrsuit.mapservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "geo_location")
public class GeoLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long petId;

    private Long sightingId;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    private Double radius;

    private LocalDateTime createdAt;
}
