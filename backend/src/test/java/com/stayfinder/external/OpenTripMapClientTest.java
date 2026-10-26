package com.stayfinder.external;

import com.stayfinder.dto.NearbyPlaceDto;
import com.stayfinder.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenTripMapClientTest {

    private MockRestServiceServer mockServer;
    private RestClient.Builder builder;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
    }

    @Test
    @DisplayName("Cand cheia de API lipseste, returneaza lista goala fara apel HTTP")
    void returnsEmptyListWhenApiKeyMissing() {
        OpenTripMapClient client = new OpenTripMapClient(builder, "https://api.opentripmap.com/0.1/en", "");

        List<NearbyPlaceDto> places = client.getNearbyPlaces(45.65, 25.60);

        assertThat(places).isEmpty();
        mockServer.verify(); // nicio cerere HTTP nu a fost facuta
    }

    @Test
    @DisplayName("Cand cheia exista, apeleaza API-ul, filtreaza intrarile fara nume si mapeaza categoriile")
    void parsesPlacesWhenKeyConfigured() {
        OpenTripMapClient client = new OpenTripMapClient(builder, "https://api.opentripmap.com/0.1/en", "test-key-123");

        String jsonResponse = """
                [
                  {
                    "xid": "W123",
                    "name": "Biserica Neagra",
                    "dist": 1420.35,
                    "kinds": "historic,churches"
                  },
                  {
                    "xid": "N456",
                    "name": "",
                    "dist": 200.0,
                    "kinds": "other"
                  },
                  {
                    "xid": "W789",
                    "name": "Parcul Central",
                    "dist": 500.22,
                    "kinds": "natural,parks"
                  }
                ]
                """;

        mockServer.expect(requestTo(containsString("/places/radius?radius=5000")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(requestTo(containsString("apikey=test-key-123")))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<NearbyPlaceDto> places = client.getNearbyPlaces(45.65, 25.60);

        mockServer.verify();
        // A filtrat elementul cu nume vid ("")
        assertThat(places).hasSize(2);
        assertThat(places.get(0).name()).isEqualTo("Biserica Neagra");
        assertThat(places.get(0).category()).isEqualTo("Istoric");
        assertThat(places.get(0).distanceMeters()).isEqualTo(1420.4);

        assertThat(places.get(1).name()).isEqualTo("Parcul Central");
        assertThat(places.get(1).category()).isEqualTo("Natura");
    }

    @Test
    @DisplayName("Cand cheia exista si serverul da eroare 500, arunca ExternalServiceException")
    void throwsWhenServerFails() {
        OpenTripMapClient client = new OpenTripMapClient(builder, "https://api.opentripmap.com/0.1/en", "test-key-123");

        mockServer.expect(requestTo(containsString("/places/radius")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.getNearbyPlaces(45.65, 25.60))
                .isInstanceOf(ExternalServiceException.class);
    }
}
