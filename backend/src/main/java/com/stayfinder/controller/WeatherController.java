package com.stayfinder.controller;

import com.stayfinder.dto.WeatherForecastDto;
import com.stayfinder.external.OpenMeteoClient;
import com.stayfinder.model.Accommodation;
import com.stayfinder.service.AccommodationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint pentru prognoza meteo a unei cazări.
 *
 * Apelul către API-ul extern Open-Meteo se face prin backend pentru a nu expune
 * logica externă către Android și pentru a transforma datele într-un format simplificat.
 */
@RestController
@RequestMapping("/api/accommodations/{id}/weather")
@Tag(name = "Vreme", description = "Prognoza meteo pentru destinația cazării (Open-Meteo)")
public class WeatherController {

    private final AccommodationService accommodationService;
    private final OpenMeteoClient openMeteoClient;

    public WeatherController(AccommodationService accommodationService, OpenMeteoClient openMeteoClient) {
        this.accommodationService = accommodationService;
        this.openMeteoClient = openMeteoClient;
    }

    @GetMapping
    @Operation(summary = "Prognoza meteo curentă pe 7 zile pentru cazarea specificată")
    public WeatherForecastDto getWeather(
            @Parameter(description = "ID-ul cazării") @PathVariable Long id
    ) {
        Accommodation accommodation = accommodationService.findEntity(id);
        String locationName = accommodation.getTitle() + " (" + accommodation.getCity() + ")";
        return openMeteoClient.getForecast(
                id,
                locationName,
                accommodation.getLatitude(),
                accommodation.getLongitude()
        );
    }
}
