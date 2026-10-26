package com.stayfinder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configurează un RestClient.Builder cu timeout-uri clare de conectare și citire (5 secunde).
 *
 * De ce: dacă un serviciu extern (Open-Meteo, Frankfurter etc.) este lent sau inaccesibil,
 * nu dorim ca cererile utilizatorilor să rămână blocate la nesfârșit consumând resursele serverului.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .requestFactory(requestFactory);
    }
}
