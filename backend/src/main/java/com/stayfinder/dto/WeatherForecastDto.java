package com.stayfinder.dto;

import java.util.List;

/**
 * Prognoza meteo completă pe 7 zile pentru o cazare.
 *
 * @param accommodationId ID-ul cazării asociate
 * @param locationName Numele locației (titlu sau oraș)
 * @param note Notă informativă pentru client (ex. prognoză curentă pe 7 zile)
 * @param days Lista prognozelor zilnice
 */
public record WeatherForecastDto(
        Long accommodationId,
        String locationName,
        String note,
        List<DailyWeatherDto> days
) {
}
