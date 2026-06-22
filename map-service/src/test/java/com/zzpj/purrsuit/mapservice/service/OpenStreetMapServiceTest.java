package com.zzpj.purrsuit.mapservice.service;

import com.zzpj.purrsuit.mapservice.domain.GeocodingResult;
import com.zzpj.purrsuit.mapservice.domain.Success;
import com.zzpj.purrsuit.mapservice.domain.Failure;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

@RestClientTest(OpenStreetMapService.class)
@TestPropertySource(properties = "osm.email=test@example.com") // Wstrzykujemy wartość dla @Value
class OpenStreetMapServiceTest {

    @Autowired
    private OpenStreetMapService service;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void shouldReturnSuccessWhenAddressIsFound() {
        // given
        String address = "Łódź, Piotrkowska";
        String mockJsonResponse = """
                [
                    {
                        "display_name": "Piotrkowska, Łódź, Polska",
                        "lat": "51.765",
                        "lon": "19.456"
                    }
                ]
                """;

        server.expect(requestTo("https://nominatim.openstreetmap.org/search?q=%C5%81%C3%B3d%C5%BA,%20Piotrkowska&format=json&limit=1"))
                .andRespond(withSuccess(mockJsonResponse, MediaType.APPLICATION_JSON));

        // when
        GeocodingResult result = service.geocodeAddress(address);

        // then
        assertThat(result).isInstanceOf(Success.class);
        Success success = (Success) result;
        assertThat(success.location().address()).isEqualTo("Piotrkowska, Łódź, Polska");
        assertThat(success.location().latitude()).isEqualTo(51.765);
        assertThat(success.location().longitude()).isEqualTo(19.456);
    }

    @Test
    void shouldReturnFailureWhenApiReturnsEmptyArray() {
        // given
        String address = "Nieistniejący Adres";
        String mockEmptyResponse = "[]";

        server.expect(requestTo("https://nominatim.openstreetmap.org/search?q=Nieistniej%C4%85cy%20Adres&format=json&limit=1"))
                .andRespond(withSuccess(mockEmptyResponse, MediaType.APPLICATION_JSON));

        // when
        GeocodingResult result = service.geocodeAddress(address);

        // then
        assertThat(result).isInstanceOf(Failure.class);
        assertThat(((Failure) result).reason()).isEqualTo("Address not found");
    }

    @Test
    void shouldReturnFailureWhenApiThrowsException() {
        // given
        String address = "Error Street";

        server.expect(requestTo("https://nominatim.openstreetmap.org/search?q=Error%20Street&format=json&limit=1"))
                .andRespond(withServerError()); // Zwróci np. 500 Internal Server Error

        // when
        GeocodingResult result = service.geocodeAddress(address);

        // then
        assertThat(result).isInstanceOf(Failure.class);
        // Wiadomość błędu zależy od implementacji RestClienta, ale upewniamy się, że serwis złapał wyjątek
        assertThat(((Failure) result).reason()).contains("500");
    }
}