package com.stayfinder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stayfinder.model.Accommodation;
import com.stayfinder.model.AccommodationType;
import com.stayfinder.model.TravelType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ce trimitem clientului (Android / web) despre o cazare.
 * În feed-ul personalizat, include suplimentar câmpurile `score` și `reason`.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AccommodationDto(
        Long id,
        String title,
        String description,
        String country,
        String region,
        String city,
        AccommodationType type,
        BigDecimal pricePerNight,
        boolean allInclusive,
        double rating,
        String imageUrl,
        double latitude,
        double longitude,
        List<String> tags,
        TravelType suitableFor,
        Double score,
        String reason
) {

    /** Conversie fără scor și motiv (pentru listări publice). */
    public static AccommodationDto from(Accommodation a) {
        return from(a, null, null);
    }

    /** Conversie completă cu scor de relevanță și motivul recomandării (pentru feed). */
    public static AccommodationDto from(Accommodation a, Double score, String reason) {
        return new AccommodationDto(
                a.getId(),
                a.getTitle(),
                a.getDescription(),
                a.getCountry(),
                a.getRegion(),
                a.getCity(),
                a.getType(),
                a.getPricePerNight(),
                a.isAllInclusive(),
                a.getRating(),
                a.getImageUrl(),
                a.getLatitude(),
                a.getLongitude(),
                List.copyOf(a.getTags()),
                a.getSuitableFor(),
                score,
                reason
        );
    }
}
