package com.stayfinder.controller;

import com.stayfinder.dto.ExchangeRatesDto;
import com.stayfinder.dto.NearbyPlaceDto;
import com.stayfinder.dto.WeatherForecastDto;
import com.stayfinder.external.FrankfurterClient;
import com.stayfinder.external.OpenMeteoClient;
import com.stayfinder.external.OpenTripMapClient;
import com.stayfinder.model.Accommodation;
import com.stayfinder.service.AccommodationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * Controller Web MVC pentru randarea paginii de detaliu a cazării în Thymeleaf (Faza 5C).
 *
 * Principiul "Server-Side Rendering":
 * - Nu se fac apeluri JavaScript către API-urile externe din browser.
 * - Toate datele (vreme, curs valutar din cache, atracții turistice) sunt agregate pe server
 *   prin serviciile interne și trimise ca HTML gata randat.
 */
@Controller
@RequestMapping("/accommodations")
public class WebAccommodationController {

    private static final Logger log = LoggerFactory.getLogger(WebAccommodationController.class);

    private final AccommodationService accommodationService;
    private final OpenMeteoClient openMeteoClient;
    private final FrankfurterClient frankfurterClient;
    private final OpenTripMapClient openTripMapClient;

    public WebAccommodationController(AccommodationService accommodationService,
                                      OpenMeteoClient openMeteoClient,
                                      FrankfurterClient frankfurterClient,
                                      OpenTripMapClient openTripMapClient) {
        this.accommodationService = accommodationService;
        this.openMeteoClient = openMeteoClient;
        this.frankfurterClient = frankfurterClient;
        this.openTripMapClient = openTripMapClient;
    }

    @GetMapping("/{id}")
    public String getDetailPage(@PathVariable Long id, Model model) {
        Accommodation accommodation = accommodationService.findEntity(id);
        model.addAttribute("accommodation", accommodation);

        // 1. Curs valutar & calcul preț aproximativ în RON
        Double ronRate = null;
        BigDecimal priceRon = null;
        try {
            ExchangeRatesDto rates = frankfurterClient.getExchangeRates("EUR");
            if (rates != null && rates.rates() != null) {
                ronRate = rates.rates().get("RON");
                if (ronRate != null && accommodation.getPricePerNight() != null) {
                    priceRon = accommodation.getPricePerNight()
                            .multiply(BigDecimal.valueOf(ronRate))
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }
        } catch (Exception ex) {
            log.warn("Nu s-a putut obține cursul valutar pentru detaliul web al cazării {}: {}", id, ex.getMessage());
        }
        model.addAttribute("ronRate", ronRate != null ? String.format(Locale.US, "%.4f", ronRate) : null);
        model.addAttribute("priceRon", priceRon != null ? String.format(Locale.US, "%.2f", priceRon) : null);

        // 2. Prognoza meteo pe 7 zile (Open-Meteo)
        WeatherForecastDto weather = null;
        try {
            String locationName = accommodation.getTitle() + " (" + accommodation.getCity() + ")";
            weather = openMeteoClient.getForecast(
                    id,
                    locationName,
                    accommodation.getLatitude(),
                    accommodation.getLongitude()
            );
        } catch (Exception ex) {
            log.warn("Nu s-a putut obține prognoza meteo pentru cazarea {}: {}", id, ex.getMessage());
        }
        model.addAttribute("weather", weather);

        // 3. Obiective turistice în apropiere (OpenTripMap)
        List<NearbyPlaceDto> nearbyPlaces = List.of();
        try {
            nearbyPlaces = openTripMapClient.getNearbyPlaces(
                    accommodation.getLatitude(),
                    accommodation.getLongitude()
            );
        } catch (Exception ex) {
            log.warn("Nu s-au putut obține obiectivele turistice pentru cazarea {}: {}", id, ex.getMessage());
        }
        model.addAttribute("nearbyPlaces", nearbyPlaces);

        return "accommodation-detail";
    }
}
