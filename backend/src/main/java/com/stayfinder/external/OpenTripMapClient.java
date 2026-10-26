package com.stayfinder.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stayfinder.dto.NearbyPlaceDto;
import com.stayfinder.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

/**
 * Client HTTP pentru OpenTripMap (obiective turistice în apropiere).
 *
 * Funcționalitate opțională:
 * - Dacă cheia de API (OPENTRIPMAP_API_KEY) nu este configurată sau este goală,
 *   clientul întoarce o listă vidă fără a efectua niciun apel de rețea.
 * - Dacă cheia este furnizată, interoghează obiectivele pe o rază de 5 km și returnează primele maxim 10.
 */
@Component
public class OpenTripMapClient {

    private static final Logger log = LoggerFactory.getLogger(OpenTripMapClient.class);

    private final RestClient restClient;
    private final String baseUrl;
    private final String apiKey;

    public OpenTripMapClient(RestClient.Builder restClientBuilder,
                             @Value("${app.external.opentripmap.base-url:https://api.opentripmap.com/0.1/en}") String baseUrl,
                             @Value("${app.external.opentripmap.api-key:}") String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public record OpenTripMapItem(
            String xid,
            String name,
            double dist,
            String kinds
    ) {}

    /**
     * Verifică dacă cheia de API este configurată și nu e doar un șablon de exemplu.
     */
    public boolean isConfigured() {
        return !apiKey.isBlank() && !apiKey.contains("your_") && !apiKey.equalsIgnoreCase("placeholder");
    }

    /**
     * Caută obiective turistice pe o rază de 5000m în jurul coordonatelor date.
     * Dacă serviciul nu este configurat, returnează imediat o listă goală.
     */
    public List<NearbyPlaceDto> getNearbyPlaces(double latitude, double longitude) {
        if (!isConfigured()) {
            log.debug("OpenTripMap API key nu este configurată. Returnăm listă vidă.");
            return List.of();
        }

        // radius=5000 (5 km), limit=20 (pentru a avea rezerve după filtrarea celor fără nume)
        String uri = String.format(
                java.util.Locale.ROOT,
                "/places/radius?radius=5000&lon=%.4f&lat=%.4f&limit=20&format=json&apikey=%s",
                longitude, latitude, apiKey
        );

        try {
            List<OpenTripMapItem> items = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<OpenTripMapItem>>() {});

            if (items == null || items.isEmpty()) {
                return List.of();
            }

            return items.stream()
                    .filter(item -> item.name() != null && !item.name().isBlank())
                    .limit(10)
                    .map(item -> new NearbyPlaceDto(
                            item.name().trim(),
                            extractCategory(item.kinds()),
                            Math.round(item.dist() * 10.0) / 10.0
                    ))
                    .toList();
        } catch (RestClientException ex) {
            log.warn("Eroare la apelarea OpenTripMap pentru lat={}, lon={}: {}", latitude, longitude, ex.getMessage());
            throw new ExternalServiceException("Serviciul de obiective turistice este indisponibil: " + ex.getMessage(), ex);
        }
    }

    /**
     * Extrage prima categorie relevantă din etichetele kinds separate prin virgulă.
     */
    private String extractCategory(String kinds) {
        if (kinds == null || kinds.isBlank()) {
            return "atractie";
        }
        String first = kinds.split(",")[0].trim().toLowerCase();
        return switch (first) {
            case "historic", "architecture" -> "Istoric";
            case "cultural", "museums" -> "Cultural";
            case "natural" -> "Natura";
            case "religion" -> "Religios";
            case "amusements" -> "Divertisment";
            default -> first;
        };
    }
}
