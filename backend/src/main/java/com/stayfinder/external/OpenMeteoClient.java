package com.stayfinder.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stayfinder.dto.DailyWeatherDto;
import com.stayfinder.dto.WeatherForecastDto;
import com.stayfinder.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Client HTTP pentru serviciul public Open-Meteo (fără cheie API).
 *
 * De ce apelăm din backend:
 * 1. Aplicația Android nu depinde de URL-ul extern și nu are nevoie de logică de parsare meteo.
 * 2. Putem adăuga cache sau transforma datele într-un format simplu și uniform pentru UI.
 */
@Component
public class OpenMeteoClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    public OpenMeteoClient(RestClient.Builder restClientBuilder,
                           @Value("${app.external.open-meteo.base-url:https://api.open-meteo.com/v1}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * DTO-uri interne pentru maparea răspunsului JSON brut returnat de Open-Meteo.
     */
    public record OpenMeteoDaily(
            @JsonProperty("time") List<String> time,
            @JsonProperty("weather_code") List<Integer> weatherCode,
            @JsonProperty("weathercode") List<Integer> weathercodeAlias,
            @JsonProperty("temperature_2m_max") List<Double> tempMax,
            @JsonProperty("temperature_2m_min") List<Double> tempMin,
            @JsonProperty("precipitation_sum") List<Double> precipitationSum
    ) {}

    public record OpenMeteoResponse(
            double latitude,
            double longitude,
            String timezone,
            OpenMeteoDaily daily
    ) {}

    /**
     * Obține prognoza pe 7 zile pentru coordonatele date.
     */
    public WeatherForecastDto getForecast(Long accommodationId, String locationName, double latitude, double longitude) {
        String uri = String.format(
                java.util.Locale.ROOT,
                "/forecast?latitude=%.4f&longitude=%.4f&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum&timezone=auto",
                latitude, longitude
        );

        try {
            OpenMeteoResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(OpenMeteoResponse.class);

            if (response == null || response.daily() == null || response.daily().time() == null) {
                throw new ExternalServiceException("Răspuns gol sau invalid primit de la Open-Meteo");
            }

            OpenMeteoDaily daily = response.daily();
            List<String> dates = daily.time();
            List<Integer> codes = daily.weatherCode() != null ? daily.weatherCode() : daily.weathercodeAlias();
            List<Double> maxTemps = daily.tempMax();
            List<Double> minTemps = daily.tempMin();
            List<Double> precipitations = daily.precipitationSum();

            int count = dates.size();
            List<DailyWeatherDto> days = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                LocalDate date = LocalDate.parse(dates.get(i));
                int code = (codes != null && i < codes.size() && codes.get(i) != null) ? codes.get(i) : 0;
                double max = (maxTemps != null && i < maxTemps.size() && maxTemps.get(i) != null) ? maxTemps.get(i) : 0.0;
                double min = (minTemps != null && i < minTemps.size() && minTemps.get(i) != null) ? minTemps.get(i) : 0.0;
                double precip = (precipitations != null && i < precipitations.size() && precipitations.get(i) != null) ? precipitations.get(i) : 0.0;

                WeatherCodeMapper.WeatherInfo info = WeatherCodeMapper.map(code);
                days.add(new DailyWeatherDto(date, min, max, precip, info.description(), info.icon()));
            }

            return new WeatherForecastDto(
                    accommodationId,
                    locationName,
                    "Prognoza meteo curenta pentru destinatie (urmatoarele 7 zile)",
                    days
            );
        } catch (RestClientException ex) {
            log.error("Eroare la apelarea Open-Meteo pentru lat={}, lon={}: {}", latitude, longitude, ex.getMessage());
            throw new ExternalServiceException("Serviciul meteo extern este indisponibil momentan: " + ex.getMessage(), ex);
        }
    }
}
