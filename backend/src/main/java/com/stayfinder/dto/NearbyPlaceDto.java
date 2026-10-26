package com.stayfinder.dto;

/**
 * Punct de interes turistic din apropierea cazării (furnizat opțional de OpenTripMap).
 *
 * @param name Numele obiectivului turistic
 * @param category Categoria (ex. "historic", "museum", "natural")
 * @param distanceMeters Distanța aproximativă față de cazare, în metri
 */
public record NearbyPlaceDto(
        String name,
        String category,
        double distanceMeters
) {
}
