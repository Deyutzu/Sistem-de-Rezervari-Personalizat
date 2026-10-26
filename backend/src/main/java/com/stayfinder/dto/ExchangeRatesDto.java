package com.stayfinder.dto;

import java.time.LocalDate;
import java.util.Map;

/**
 * Răspunsul pentru cursurile de schimb valutar.
 *
 * @param base Moneda de referință (ex. "EUR")
 * @param date Data cursului valutar
 * @param rates Ratele de schimb către alte monede (ex. RON, USD, GBP)
 */
public record ExchangeRatesDto(
        String base,
        LocalDate date,
        Map<String, Double> rates
) {
}
