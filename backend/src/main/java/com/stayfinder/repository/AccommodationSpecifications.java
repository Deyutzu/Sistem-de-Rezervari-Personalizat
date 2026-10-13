package com.stayfinder.repository;

import com.stayfinder.model.Accommodation;
import com.stayfinder.model.AccommodationType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Filtre reutilizabile pentru căutarea de cazări.
 *
 * De ce Specification și nu o singură @Query cu "(:x IS NULL OR ...)":
 * fiecare filtru e o bucată mică, independentă, pe care o adăugăm DOAR dacă
 * parametrul a fost trimis. Rezultă SQL curat și cod ușor de citit/testat.
 * Fiecare metodă întoarce null când filtrul nu se aplică; Spring Data ignoră
 * specificațiile null la combinare (and).
 */
public final class AccommodationSpecifications {

    private AccommodationSpecifications() {
    }

    public static Specification<Accommodation> hasType(AccommodationType type) {
        return type == null ? null : (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Accommodation> priceAtLeast(BigDecimal min) {
        return min == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("pricePerNight"), min);
    }

    public static Specification<Accommodation> priceAtMost(BigDecimal max) {
        return max == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("pricePerNight"), max);
    }

    public static Specification<Accommodation> isAllInclusive(Boolean allInclusive) {
        return allInclusive == null ? null : (root, query, cb) -> cb.equal(root.get("allInclusive"), allInclusive);
    }

    public static Specification<Accommodation> inCountry(String country) {
        if (country == null || country.isBlank()) {
            return null;
        }
        // Comparație case-insensitive: "italia" == "Italia"
        return (root, query, cb) -> cb.equal(cb.lower(root.get("country")), country.trim().toLowerCase());
    }

    /** Căutare text liberă în titlu, oraș, regiune și țară (LIKE %text%). */
    public static Specification<Accommodation> matchesSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("city")), pattern),
                cb.like(cb.lower(root.get("region")), pattern),
                cb.like(cb.lower(root.get("country")), pattern)
        );
    }
}
