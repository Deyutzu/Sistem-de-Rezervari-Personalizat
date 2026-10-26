package com.stayfinder.controller;

import com.stayfinder.dto.DailyWeatherDto;
import com.stayfinder.dto.ExchangeRatesDto;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class WebAccommodationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpenMeteoClient openMeteoClient;

    @MockitoBean
    private FrankfurterClient frankfurterClient;

    @MockitoBean
    private OpenTripMapClient openTripMapClient;

    @Test
    @DisplayName("GET /accommodations/1 randeaza pagina Thymeleaf cu entitatea reala din baza de date")
    void testGetDetailPageRealDatabase() throws Exception {
        ExchangeRatesDto exchangeRatesDto = new ExchangeRatesDto(
                "EUR",
                LocalDate.now(),
                Map.of("RON", 4.975)
        );
        when(frankfurterClient.getExchangeRates("EUR")).thenReturn(exchangeRatesDto);

        WeatherForecastDto forecastDto = new WeatherForecastDto(
                1L,
                "Hotel Brasov",
                "Prognoza 7 zile",
                List.of(new DailyWeatherDto(LocalDate.now(), 5.0, 15.0, 0.0, "Senin", "sun"))
        );
        when(openMeteoClient.getForecast(anyLong(), anyString(), anyDouble(), anyDouble()))
                .thenReturn(forecastDto);

        when(openTripMapClient.getNearbyPlaces(anyDouble(), anyDouble()))
                .thenReturn(List.of());

        mockMvc.perform(get("/accommodations/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("accommodation-detail"))
                .andExpect(model().attributeExists("accommodation"));
    }
}
