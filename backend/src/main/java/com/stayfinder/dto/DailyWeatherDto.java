package com.stayfinder.dto;

import java.time.LocalDate;

/**
 * Prognoza meteo pentru o singură zi.
 *
 * @param date Data prognozei
 * @param tempMin Temperatura minimă în °C
 * @param tempMax Temperatura maximă în °C
 * @param precipitation Cantitatea totală de precipitații în mm
 * @param description Descrierea stării vremii în limba română (ex. "Senin", "Ploaie")
 * @param icon Numele iconiței standard ("sun", "clouds", "rain", "snow", "thunder")
 */
public record DailyWeatherDto(
        LocalDate date,
        double tempMin,
        double tempMax,
        double precipitation,
        String description,
        String icon
) {
}
