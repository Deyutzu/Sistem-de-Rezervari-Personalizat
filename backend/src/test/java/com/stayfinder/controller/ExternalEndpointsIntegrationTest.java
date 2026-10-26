package com.stayfinder.controller;

import com.stayfinder.dto.DailyWeatherDto;
import com.stayfinder.dto.ExchangeRatesDto;
import com.stayfinder.dto.NearbyPlaceDto;
import com.stayfinder.dto.WeatherForecastDto;
import com.stayfinder.external.FrankfurterClient;
import com.stayfinder.external.OpenMeteoClient;
import com.stayfinder.external.OpenTripMapClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExternalEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpenMeteoClient openMeteoClient;

    @MockitoBean
    private FrankfurterClient frankfurterClient;

    @MockitoBean
    private OpenTripMapClient openTripMapClient;

    @Test
    @DisplayName("GET /api/exchange-rates este public si returneaza ratele de schimb")
    void getExchangeRatesPublic() throws Exception {
        ExchangeRatesDto mockDto = new ExchangeRatesDto(
                "EUR",
                LocalDate.of(2026, 10, 24),
                Map.of("RON", 4.975, "USD", 1.085)
        );

        when(frankfurterClient.getExchangeRates("EUR")).thenReturn(mockDto);

        mockMvc.perform(get("/api/exchange-rates?base=EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base").value("EUR"))
                .andExpect(jsonPath("$.rates.RON").value(4.975))
                .andExpect(jsonPath("$.rates.USD").value(1.085));
    }

    @Test
    @DisplayName("GET /api/accommodations/{id}/weather este public si returneaza prognoza")
    void getWeatherPublic() throws Exception {
        WeatherForecastDto mockForecast = new WeatherForecastDto(
                1L,
                "Locatie Test",
                "Prognoza 7 zile",
                List.of(new DailyWeatherDto(LocalDate.of(2026, 10, 26), 5.0, 15.0, 0.0, "Senin", "sun"))
        );

        when(openMeteoClient.getForecast(anyLong(), anyString(), anyDouble(), anyDouble()))
                .thenReturn(mockForecast);

        mockMvc.perform(get("/api/accommodations/1/weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accommodationId").value(1))
                .andExpect(jsonPath("$.days[0].description").value("Senin"))
                .andExpect(jsonPath("$.days[0].icon").value("sun"));
    }

    @Test
    @DisplayName("GET /api/accommodations/99999/weather returneaza 404 cand cazarea nu exista")
    void getWeatherNotFound() throws Exception {
        mockMvc.perform(get("/api/accommodations/99999/weather"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/accommodations/{id}/nearby returneaza lista de obiective")
    void getNearbyPlacesPublic() throws Exception {
        List<NearbyPlaceDto> mockPlaces = List.of(
                new NearbyPlaceDto("Castelul Bran", "Istoric", 1250.0)
        );

        when(openTripMapClient.getNearbyPlaces(anyDouble(), anyDouble())).thenReturn(mockPlaces);

        mockMvc.perform(get("/api/accommodations/1/nearby"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Castelul Bran"))
                .andExpect(jsonPath("$[0].category").value("Istoric"))
                .andExpect(jsonPath("$[0].distanceMeters").value(1250.0));
    }
}
