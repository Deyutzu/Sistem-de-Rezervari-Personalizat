package com.stayfinder.external;

import com.stayfinder.dto.WeatherForecastDto;
import com.stayfinder.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenMeteoClientTest {

    private MockRestServiceServer mockServer;
    private OpenMeteoClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new OpenMeteoClient(builder, "https://api.open-meteo.com/v1");
    }

    @Test
    @DisplayName("getForecast parseaza cu succes raspunsul Open-Meteo pe 7 zile")
    void getForecastSuccess() {
        String jsonResponse = """
                {
                  "latitude": 45.65,
                  "longitude": 25.60,
                  "timezone": "Europe/Bucharest",
                  "daily": {
                    "time": ["2026-10-26", "2026-10-27"],
                    "weather_code": [0, 61],
                    "temperature_2m_max": [18.5, 14.0],
                    "temperature_2m_min": [6.2, 8.1],
                    "precipitation_sum": [0.0, 4.5]
                  }
                }
                """;

        mockServer.expect(requestTo(containsString("/forecast?latitude=45.6500&longitude=25.6000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        WeatherForecastDto forecast = client.getForecast(1L, "Hotel Brasov", 45.65, 25.60);

        mockServer.verify();
        assertThat(forecast.accommodationId()).isEqualTo(1L);
        assertThat(forecast.locationName()).isEqualTo("Hotel Brasov");
        assertThat(forecast.days()).hasSize(2);

        assertThat(forecast.days().get(0).description()).isEqualTo("Senin");
        assertThat(forecast.days().get(0).icon()).isEqualTo("sun");
        assertThat(forecast.days().get(0).tempMax()).isEqualTo(18.5);

        assertThat(forecast.days().get(1).description()).isEqualTo("Ploaie");
        assertThat(forecast.days().get(1).icon()).isEqualTo("rain");
        assertThat(forecast.days().get(1).precipitation()).isEqualTo(4.5);
    }

    @Test
    @DisplayName("getForecast arunca ExternalServiceException cand API-ul da eroare 500")
    void getForecastServerError() {
        mockServer.expect(requestTo(containsString("/forecast")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.getForecast(1L, "Hotel Brasov", 45.65, 25.60))
                .isInstanceOf(ExternalServiceException.class);

        mockServer.verify();
    }
}
