package com.zzpj.purrsuit.mapservice.controller;

import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.mapservice.domain.Failure;
import com.zzpj.purrsuit.mapservice.domain.LocationDto;
import com.zzpj.purrsuit.mapservice.domain.SaveLocationRequest;
import com.zzpj.purrsuit.mapservice.domain.Success;
import com.zzpj.purrsuit.mapservice.entity.GeoLocation;
import com.zzpj.purrsuit.mapservice.service.LocationMatchingService;
import com.zzpj.purrsuit.mapservice.service.OpenStreetMapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapControllerTest {

    @Mock
    private LocationMatchingService locationService;

    @Mock
    private OpenStreetMapService mapClient;

    @InjectMocks
    private MapController controller;

    @Test
    void shouldReturnBadRequestWhenAddressIsEmpty() {
        // given
        Map<String, String> payload = Map.of("address", "   ");

        // when
        ResponseEntity<?> response = controller.getGeocode(payload);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Błąd: Pole 'address' nie może być puste");
    }

    @Test
    void shouldReturnLocationWhenGeocodeIsSuccessful() {
        // given
        Map<String, String> payload = Map.of("address", "Łódź, Piotrkowska");
        LocationDto locationDto = new LocationDto("Łódź, Piotrkowska", 51.75, 19.45);
        when(mapClient.geocodeAddress("Łódź, Piotrkowska")).thenReturn(new Success(locationDto));

        // when
        ResponseEntity<?> response = controller.getGeocode(payload);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(locationDto);
    }

    @Test
    void shouldReturnBadRequestWhenGeocodeFails() {
        // given
        Map<String, String> payload = Map.of("address", "NieistniejacyAdres123");
        when(mapClient.geocodeAddress("NieistniejacyAdres123")).thenReturn(new Failure("Address not found"));

        // when
        ResponseEntity<?> response = controller.getGeocode(payload);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Błąd: Address not found");
    }

    @Test
    void shouldSaveLocation() {
        // given
        UUID noticeId = UUID.randomUUID();
        SaveLocationRequest request = new SaveLocationRequest(noticeId, "FOUND", "cat",51.0, 19.0, 10.0, NoticeStatus.ACTIVE);
        GeoLocation mockGeoLocation = GeoLocation.builder().noticeId(noticeId).build();

        when(locationService.save(noticeId, "FOUND", "cat",51.0, 19.0, 10.0, NoticeStatus.ACTIVE)).thenReturn(mockGeoLocation);

        // when
        ResponseEntity<GeoLocation> response = controller.saveLocation(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockGeoLocation);
        verify(locationService).save(noticeId, "FOUND", "cat",51.0, 19.0, 10.0, NoticeStatus.ACTIVE);
    }

    @Test
    void shouldGetLocationsNear() {
        // given
        when(locationService.getLocationsNear(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Collections.emptyList());

        // when
        ResponseEntity<List<GeoLocation>> response = controller.getLocationsNear(51.0, 19.0, 5.0);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldGetMatches() {
        // given
        UUID noticeId = UUID.randomUUID();
        when(locationService.findMatchesForNotice(noticeId, "CAT", 3))
                .thenReturn(Collections.emptyList());

        // when
        ResponseEntity<List<GeoLocation>> response = controller.getMatches(noticeId, "CAT", 3);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}