package com.stayfinder.external;

import com.stayfinder.dto.ExchangeRatesDto;
import com.stayfinder.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FrankfurterClientTest {

    private MockRestServiceServer mockServer;
    private FrankfurterClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new FrankfurterClient(builder, "https://api.frankfurter.dev/v1");
    }

    @Test
    @DisplayName("getExchangeRates returneaza cursurile de schimb pentru EUR")
    void getExchangeRatesSuccess() {
        String jsonResponse = """
                {
                  "amount": 1.0,
                  "base": "EUR",
                  "date": "2026-10-24",
                  "rates": {
                    "RON": 4.975,
                    "USD": 1.085,
                    "GBP": 0.850
                  }
                }
                """;

        mockServer.expect(requestTo(containsString("/latest?base=EUR")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ExchangeRatesDto rates = client.getExchangeRates("EUR");

        mockServer.verify();
        assertThat(rates.base()).isEqualTo("EUR");
        assertThat(rates.rates()).containsEntry("RON", 4.975);
        assertThat(rates.rates()).containsEntry("USD", 1.085);
        assertThat(rates.rates()).containsEntry("GBP", 0.850);
    }

    @Test
    @DisplayName("Cand API-ul extern pica dupa un apel reusit, foloseste cursul anterior de fallback")
    void fallbackWhenApiFails() {
        String jsonResponse = """
                {
                  "amount": 1.0,
                  "base": "EUR",
                  "date": "2026-10-24",
                  "rates": {
                    "RON": 4.975
                  }
                }
                """;

        // Primul apel reuseste
        mockServer.expect(requestTo(containsString("/latest?base=EUR")))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ExchangeRatesDto initial = client.getExchangeRates("EUR");
        assertThat(initial.rates()).containsEntry("RON", 4.975);

        // Al doilea apel esueaza la nivel de server extern
        mockServer.reset();
        mockServer.expect(requestTo(containsString("/latest?base=EUR")))
                .andRespond(withServerError());

        // Clientul trebuie sa returneze ultimul curs valid fara sa arunce exceptie
        ExchangeRatesDto fallback = client.getExchangeRates("EUR");
        assertThat(fallback).isNotNull();
        assertThat(fallback.rates()).containsEntry("RON", 4.975);
    }

    @Test
    @DisplayName("Cand API-ul pica de la inceput fara nimic in memorie, arunca ExternalServiceException")
    void throwsWhenNoFallbackAvailable() {
        mockServer.expect(requestTo(containsString("/latest?base=EUR")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.getExchangeRates("EUR"))
                .isInstanceOf(ExternalServiceException.class);
    }
}
