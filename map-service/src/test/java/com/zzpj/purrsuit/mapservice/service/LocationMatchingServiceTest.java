package com.zzpj.purrsuit.mapservice.service;

import com.zzpj.purrsuit.mapservice.domain.AreaSearchRequest;
import com.zzpj.purrsuit.mapservice.domain.PointDto;
import com.zzpj.purrsuit.mapservice.entity.GeoLocation;
import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.mapservice.repository.GeoLocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationMatchingServiceTest {

    @Mock
    private GeoLocationRepository repository;

    @Mock
    private LocationMatchKafkaProducer kafkaProducer;

    @InjectMocks
    private LocationMatchingService service;

    @Captor
    private ArgumentCaptor<List<UUID>> listCaptor;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void shouldCalculateSearchRadiusForCat() {
        // given
        String species = "CAT";
        int daysMissing = 3;

        // when
        double radius = service.calculateSearchRadius(species, daysMissing);

        // then
        // Promień dla kota: 1.0 km * 3 dni * 1000 = 3000 metrów
        assertThat(radius).isEqualTo(3000.0);
    }

    @Test
    void shouldCalculateSearchRadiusForDog() {
        // given
        String species = "DOG";
        int daysMissing = 2;

        // when
        double radius = service.calculateSearchRadius(species, daysMissing);

        // then
        // Promień dla innego gatunku (domyślnie psa): 5.0 km * 2 dni * 1000 = 10000 metrów
        assertThat(radius).isEqualTo(10000.0);
    }

    @Test
    void shouldSaveLocation() {
        // given
        UUID noticeId = UUID.randomUUID();
        double lat = 51.7592;
        double lon = 19.4559;
        GeoLocation savedLocation = GeoLocation.builder().noticeId(noticeId).build();

        when(repository.save(any(GeoLocation.class))).thenReturn(savedLocation);

        // when
        GeoLocation result = service.save(noticeId, "LOST", "kot",
                lat, lon, 50.0, NoticeStatus.ACTIVE);

        // then
        assertThat(result).isNotNull();

        // Weryfikacja argumentów przekazanych do zapisu
        ArgumentCaptor<GeoLocation> captor = ArgumentCaptor.forClass(GeoLocation.class);
        verify(repository).save(captor.capture());

        GeoLocation captured = captor.getValue();
        assertThat(captured.getNoticeId()).isEqualTo(noticeId);
        assertThat(captured.getNoticeType()).isEqualTo("LOST");
        assertThat(captured.getLocation().getX()).isEqualTo(lon);
        assertThat(captured.getLocation().getY()).isEqualTo(lat);
    }

    @Test
    void shouldFindMatchesForNoticeAndSendToKafka() {
        // given
        UUID originNoticeId = UUID.randomUUID();
        UUID nearbyNoticeId = UUID.randomUUID();

        Point originPoint = geometryFactory.createPoint(new Coordinate(19.4559, 51.7592));
        GeoLocation originLocation = GeoLocation.builder()
        .noticeId(originNoticeId)
        .location(originPoint)
        .species("CAT") // <-- Dodaj gatunek
        .noticeStatus(NoticeStatus.ACTIVE) // <-- Dodaj status (jeśli serwis tego wymaga)
        .build();

        GeoLocation nearbyLocation = GeoLocation.builder()
        .noticeId(nearbyNoticeId)
        .species("CAT") // <-- Dodaj gatunek
        .noticeStatus(NoticeStatus.ACTIVE)
        .build();

        when(repository.findByNoticeId(originNoticeId)).thenReturn(Optional.of(originLocation));
        when(repository.findWithinRadiusAndCriteria(
                anyDouble(), anyDouble(), anyDouble(), any(), any()))
                .thenReturn(List.of(originLocation, nearbyLocation));
        // when
        List<GeoLocation> matches = service.findMatchesForNotice(originNoticeId, "CAT", 2);

        // then
        assertThat(matches  ).hasSize(2); // Zgodnie z tym co zwraca mock

        // Sprawdzamy, czy do Kafki wysłano tylko INNE ID (bez originNoticeId)
        verify(kafkaProducer).sendNearbyNotices(eq(originNoticeId), listCaptor.capture());

        List<UUID> sentIds = listCaptor.getValue();
        assertThat(sentIds).hasSize(1);
        assertThat(sentIds.get(0)).isEqualTo(nearbyNoticeId);
    }

    @Test
    void shouldThrowExceptionWhenNoticeNotFoundDuringMatching() {
        // given
        UUID noticeId = UUID.randomUUID();
        when(repository.findByNoticeId(noticeId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.findMatchesForNotice(noticeId, "DOG", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notice location not found in database");
    }

    @Test
    void shouldReturnEmptyKafkaMessageWhenNoOtherMatchesFound() {
        // given
        UUID originNoticeId = UUID.randomUUID();
        Point originPoint = geometryFactory.createPoint(new Coordinate(19.4559, 51.7592));

        GeoLocation originLocation = GeoLocation.builder()
                .noticeId(originNoticeId)
                .location(originPoint)
                .species("CAT")
                .noticeStatus(NoticeStatus.ACTIVE)
                .build();

        when(repository.findByNoticeId(originNoticeId)).thenReturn(Optional.of(originLocation));

        // Zwracamy z bazy TYLKO oryginalną lokację (brak innych zwierząt w pobliżu)
        when(repository.findWithinRadiusAndCriteria(
                anyDouble(), anyDouble(), anyDouble(), any(), any()))
                .thenReturn(List.of(originLocation));

        // when
        List<GeoLocation> matches = service.findMatchesForNotice(originNoticeId, "CAT", 2);

        // then
        assertThat(matches).hasSize(1);

        verify(kafkaProducer).sendNearbyNotices(eq(originNoticeId), listCaptor.capture());

        List<UUID> sentIds = listCaptor.getValue();
        // Sprawdzamy, czy do Kafki wysłano pustą listę (bo id równe originNoticeId zostało odfiltrowane)
        assertThat(sentIds).isEmpty();
    }

    @Test
    void shouldGetLocationsNear() {
        // given
        double lat = 51.7592;
        double lon = 19.4559;
        double radiusKm = 2.0;

        GeoLocation location = GeoLocation.builder().noticeId(UUID.randomUUID()).build();

        // Zauważ, że serwis mnoży promień * 1000.0
        when(repository.findWithinRadius(lat, lon, 2000.0)).thenReturn(List.of(location));

        // when
        List<GeoLocation> result = service.getLocationsNear(lat, lon, radiusKm);

        // then
        assertThat(result).hasSize(1);
        verify(repository).findWithinRadius(lat, lon, 2000.0);
    }

    @Test
    void shouldThrowExceptionWhenAreaHasLessThanThreePoints() {
        // given
        // Tworzymy zapytanie, które ma tylko 2 punkty
        List<PointDto> points = List.of(
                new PointDto(51.75, 19.45),
                new PointDto(51.76, 19.46)
        );
        AreaSearchRequest request = new AreaSearchRequest(points, "CAT", "LOST");

        // when & then
        assertThatThrownBy(() -> service.findMatchesWithinArea(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Wielokąt obszaru musi składać się z co najmniej 3 punktów.");
    }

    @Test
    void shouldFindMatchesWithinArea() {
        // given
        // Tworzymy kwadrat z 4 punktów
        List<PointDto> points = List.of(
                new PointDto(51.0, 19.0),
                new PointDto(51.0, 20.0),
                new PointDto(52.0, 20.0),
                new PointDto(52.0, 19.0)
        );
        AreaSearchRequest request = new AreaSearchRequest(points, "DOG", "FOUND");

        GeoLocation location = GeoLocation.builder().noticeId(UUID.randomUUID()).build();

        // Używamy anyString(), ponieważ dokładny format WKT z JTS zależy od wewnętrznej implementacji biblioteki
        when(repository.findWithinPolygon(anyString(), eq("DOG"), eq("FOUND")))
                .thenReturn(List.of(location));

        // when
        List<GeoLocation> result = service.findMatchesWithinArea(request);

        // then
        assertThat(result).hasSize(1);
        verify(repository).findWithinPolygon(anyString(), eq("DOG"), eq("FOUND"));
    }
}