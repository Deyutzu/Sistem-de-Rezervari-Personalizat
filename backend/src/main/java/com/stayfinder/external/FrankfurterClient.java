package com.stayfinder.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stayfinder.dto.ExchangeRatesDto;
import com.stayfinder.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client HTTP pentru Frankfurter (serviciu public pentru cursuri valutare de la BCE, fără cheie API).
 *
 * Strategie de reziliență:
 * 1. Spring Cache (@Cacheable cu Caffeine) păstrează răspunsul timp de 6 ore în memorie.
 * 2. Dacă API-ul extern pică și cache-ul a expirat, fallback-ul memorează ultimul curs valid cunoscut.
 * 3. Dacă nu există niciun curs cunoscut anterior, aruncă ExternalServiceException (fără să crape aplicația).
 */
@Component
public class FrankfurterClient {

    private static final Logger log = LoggerFactory.getLogger(FrankfurterClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    // Magazin local de siguranță: păstrează ultimul curs valid obținut cu succes per monedă
    private final Map<String, ExchangeRatesDto> fallbackRates = new ConcurrentHashMap<>();

    public FrankfurterClient(RestClient.Builder restClientBuilder,
                             @Value("${app.external.frankfurter.base-url:https://api.frankfurter.dev/v1}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public record FrankfurterResponse(
            double amount,
            String base,
            String date,
            Map<String, Double> rates
    ) {}

    /**
     * Returnează cursurile de schimb pentru moneda de bază specificată.
     * Rezultatul este memorat în cache-ul "exchangeRates" timp de 6 ore.
     */
    @Cacheable(value = "exchangeRates", key = "#base.toUpperCase()")
    public ExchangeRatesDto getExchangeRates(String base) {
        String baseCurrency = (base == null || base.isBlank()) ? "EUR" : base.trim().toUpperCase();

        // Determinăm simbolurile relevante de returnat
        String symbols = "EUR".equalsIgnoreCase(baseCurrency) ? "RON,USD,GBP" : "EUR,RON,USD";

        String uri = String.format("/latest?base=%s&symbols=%s", baseCurrency, symbols);

        try {
            log.info("Apelăm API extern Frankfurter pentru curs valutar: base={}", baseCurrency);
            FrankfurterResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(FrankfurterResponse.class);

            if (response == null || response.rates() == null || response.rates().isEmpty()) {
                throw new ExternalServiceException("Răspuns gol primit de la serviciul Frankfurter");
            }

            LocalDate date = response.date() != null ? LocalDate.parse(response.date()) : LocalDate.now();
            ExchangeRatesDto dto = new ExchangeRatesDto(
                    response.base(),
                    date,
                    Collections.unmodifiableMap(response.rates())
            );

            // Salvăm în memoria de fallback pentru cazurile în care API-ul va fi indisponibil în viitor
            fallbackRates.put(baseCurrency, dto);

            return dto;
        } catch (Exception ex) {
            log.warn("Eroare la apelarea Frankfurter pentru base={}: {}", baseCurrency, ex.getMessage());

            // Încercăm să folosim ultimul curs cunoscut
            ExchangeRatesDto lastKnown = fallbackRates.get(baseCurrency);
            if (lastKnown != null) {
                log.info("Folosim ultimul curs valutar salvat anterior pentru {}", baseCurrency);
                return lastKnown;
            }

            throw new ExternalServiceException("Serviciul de curs valutar este temporar indisponibil: " + ex.getMessage(), ex);
        }
    }
}
