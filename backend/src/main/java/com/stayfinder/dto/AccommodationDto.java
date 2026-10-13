package com.stayfinder.dto;

import com.stayfinder.model.Accommodation;
import com.stayfinder.model.AccommodationType;
import com.stayfinder.model.TravelType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ce trimitem clientului (Android / web) despre o cazare.
 *
 * De ce DTO și nu entitatea direct:
 *  - controlăm exact ce câmpuri apar în JSON (contractul API nu se schimbă
 *    automat când modificăm baza de date);
 *  - evităm probleme Hibernate (relații LAZY serializate în afara tranzacției,
 *    bucle infinite User -> Booking -> User);
 *  - nu expunem accidental date sensibile.
 * "record" = clasă imutabilă cu constructor, getteri, equals/hashCode generate de Java.
 */
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
        TravelType suitableFor
) {

    /** Conversie entitate -> DTO. Trebuie apelată în interiorul unei tranzacții (tags e LAZY). */
    public static AccommodationDto from(Accommodation a) {
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
                a.getSuitableFor()
        );
    }
}
