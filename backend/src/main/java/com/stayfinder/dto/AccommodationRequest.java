package com.stayfinder.dto;

import com.stayfinder.model.AccommodationType;
import com.stayfinder.model.TravelType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cerere pentru adăugarea sau modificarea unei cazări de către administrator.
 */
public record AccommodationRequest(
        @NotBlank(message = "Titlul este obligatoriu")
        @Size(max = 200, message = "Titlul nu poate depăși 200 de caractere")
        String title,

        @Size(max = 2000, message = "Descrierea nu poate depăși 2000 de caractere")
        String description,

        @NotBlank(message = "Țara este obligatorie")
        @Size(max = 100, message = "Țara nu poate depăși 100 de caractere")
        String country,

        @NotBlank(message = "Regiunea este obligatorie")
        @Size(max = 100, message = "Regiunea nu poate depăși 100 de caractere")
        String region,

        @NotBlank(message = "Orașul este obligatoriu")
        @Size(max = 100, message = "Orașul nu poate depăși 100 de caractere")
        String city,

        @NotNull(message = "Tipul cazării este obligatoriu")
        AccommodationType type,

        @NotNull(message = "Prețul per noapte este obligatoriu")
        @Positive(message = "Prețul trebuie să fie strict pozitiv")
        BigDecimal pricePerNight,

        boolean allInclusive,

        @Min(value = 0, message = "Rating-ul minim este 0")
        @Max(value = 5, message = "Rating-ul maxim este 5")
        double rating,

        @Size(max = 500, message = "URL-ul imaginii nu poate depăși 500 de caractere")
        String imageUrl,

        double latitude,
        double longitude,
        List<String> tags,
        TravelType suitableFor
) {
}
