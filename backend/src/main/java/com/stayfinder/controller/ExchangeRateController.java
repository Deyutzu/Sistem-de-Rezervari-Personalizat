package com.stayfinder.controller;

import com.stayfinder.dto.ExchangeRatesDto;
import com.stayfinder.external.FrankfurterClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint public pentru cursuri valutare.
 * Folosit de Android și Web pentru conversia prețurilor din EUR în RON (și USD/GBP).
 *
 * Răspunsurile sunt memorate în cache-ul serverului (Caffeine) timp de 6 ore.
 */
@RestController
@RequestMapping("/api/exchange-rates")
@Tag(name = "Curs Valutar", description = "Cursuri valutare de la BCE via Frankfurter (cu cache pe server)")
public class ExchangeRateController {

    private final FrankfurterClient frankfurterClient;

    public ExchangeRateController(FrankfurterClient frankfurterClient) {
        this.frankfurterClient = frankfurterClient;
    }

    @GetMapping
    @Operation(summary = "Obține cursul valutar pentru o monedă de bază (implicit EUR)")
    public ExchangeRatesDto getRates(
            @Parameter(description = "Moneda de referință (ex. EUR, USD)")
            @RequestParam(defaultValue = "EUR") String base
    ) {
        return frankfurterClient.getExchangeRates(base);
    }
}
