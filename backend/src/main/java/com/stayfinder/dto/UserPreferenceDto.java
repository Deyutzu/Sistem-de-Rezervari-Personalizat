package com.stayfinder.dto;

import com.stayfinder.model.AccommodationType;
import com.stayfinder.model.AllInclusivePreference;
import com.stayfinder.model.TravelType;
import com.stayfinder.model.UserPreference;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.Set;

public record UserPreferenceDto(
        Set<AccommodationType> preferredTypes,
        @DecimalMin(value = "0.0", message = "Bugetul minim nu poate fi negativ")
        BigDecimal budgetMin,
        @DecimalMin(value = "0.0", message = "Bugetul maxim nu poate fi negativ")
        BigDecimal budgetMax,
        AllInclusivePreference allInclusivePreference,
        TravelType travelType
) {

    public static UserPreferenceDto from(UserPreference p) {
        if (p == null) {
            return new UserPreferenceDto(Set.of(), null, null, AllInclusivePreference.ANY, null);
        }
        return new UserPreferenceDto(
                p.getPreferredTypes() != null ? Set.copyOf(p.getPreferredTypes()) : Set.of(),
                p.getBudgetMin(),
                p.getBudgetMax(),
                p.getAllInclusivePreference() != null ? p.getAllInclusivePreference() : AllInclusivePreference.ANY,
                p.getTravelType()
        );
    }
}
