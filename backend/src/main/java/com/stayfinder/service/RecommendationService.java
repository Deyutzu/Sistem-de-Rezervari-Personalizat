package com.stayfinder.service;

import com.stayfinder.dto.AccommodationDto;
import com.stayfinder.dto.PageResponse;
import com.stayfinder.model.*;
import com.stayfinder.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviciul de recomandare inteligentă a cazărilor (motorul de recomandare).
 *
 * Formula de scor:
 * score = 0.30 * typeMatch
 *       + 0.20 * budgetMatch
 *       + 0.10 * allInclusiveMatch
 *       + 0.20 * tagAffinity
 *       + 0.15 * regionDiversityBonus
 *       + 0.05 * normalizedRating
 *       - penalties
 *
 * Excluderi: cazările deja rezervate cu succes (CONFIRMED) de utilizator nu apar în feed.
 */
@Service
public class RecommendationService {

    private final AccommodationRepository accommodationRepository;
    private final BookingRepository bookingRepository;
    private final FavoriteRepository favoriteRepository;
    private final ViewEventRepository viewEventRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final SimilarRegionRepository similarRegionRepository;

    public RecommendationService(AccommodationRepository accommodationRepository,
                                 BookingRepository bookingRepository,
                                 FavoriteRepository favoriteRepository,
                                 ViewEventRepository viewEventRepository,
                                 UserPreferenceRepository userPreferenceRepository,
                                 SimilarRegionRepository similarRegionRepository) {
        this.accommodationRepository = accommodationRepository;
        this.bookingRepository = bookingRepository;
        this.favoriteRepository = favoriteRepository;
        this.viewEventRepository = viewEventRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.similarRegionRepository = similarRegionRepository;
    }

    /**
     * Structură internă ce reține o cazare alături de scorul calculat și motivul recomandării.
     */
    public record ScoredAccommodation(Accommodation accommodation, double score, String reason) {
    }

    @Transactional(readOnly = true)
    public PageResponse<AccommodationDto> getPersonalizedFeed(Long userId, int page, int size) {
        // 1. Încărcăm preferințele și istoricul de interacțiuni ale utilizatorului
        Optional<UserPreference> preferenceOpt = userPreferenceRepository.findByUserId(userId);
        List<Booking> confirmedBookings = bookingRepository.findByUserIdAndStatus(userId, BookingStatus.CONFIRMED);
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<ViewEvent> viewEvents = viewEventRepository.findByUserId(userId);

        // Set de ID-uri deja rezervate (excluse din feed)
        Set<Long> bookedAccommodationIds = confirmedBookings.stream()
                .map(b -> b.getAccommodation().getId())
                .collect(Collectors.toSet());

        // Regiuni și țări unde utilizatorul a avut rezervări confirmate
        Set<String> bookedRegions = confirmedBookings.stream()
                .map(b -> b.getAccommodation().getRegion())
                .collect(Collectors.toSet());

        Map<String, String> regionToCountryBooked = confirmedBookings.stream()
                .collect(Collectors.toMap(
                        b -> b.getAccommodation().getRegion(),
                        b -> b.getAccommodation().getCountry(),
                        (existing, replacement) -> existing
                ));

        Set<String> bookedCountries = new HashSet<>(regionToCountryBooked.values());

        // Set de ID-uri favorite
        Set<Long> favoriteAccommodationIds = favorites.stream()
                .map(f -> f.getAccommodation().getId())
                .collect(Collectors.toSet());

        // Număr de vizualizări per cazare
        Map<Long, Long> viewsPerAccommodation = viewEvents.stream()
                .collect(Collectors.groupingBy(v -> v.getAccommodation().getId(), Collectors.counting()));

        // Profilul de tag-uri al utilizatorului (ponderi: rezervare=3, favorit=2, vizualizare=1)
        Map<String, Double> userTagWeights = buildUserTagWeights(confirmedBookings, favorites, viewEvents);
        double maxUserTagWeight = userTagWeights.values().stream().max(Double::compareTo).orElse(1.0);

        // Preluăm toate similaritățile regionale existente în sistem
        List<SimilarRegion> allSimilarRegions = similarRegionRepository.findAll();

        // 2. Filtrăm și scorăm toate cazările
        List<Accommodation> allAccommodations = accommodationRepository.findAll();
        List<ScoredAccommodation> scoredList = new ArrayList<>();

        for (Accommodation acc : allAccommodations) {
            // Excludere: nu recomandăm o cazare pe care utilizatorul a rezervat-o deja
            if (bookedAccommodationIds.contains(acc.getId())) {
                continue;
            }

            ScoredAccommodation scored = computeScore(
                    acc,
                    preferenceOpt.orElse(null),
                    userTagWeights,
                    maxUserTagWeight,
                    bookedRegions,
                    bookedCountries,
                    regionToCountryBooked,
                    allSimilarRegions,
                    favoriteAccommodationIds.contains(acc.getId()),
                    viewsPerAccommodation.getOrDefault(acc.getId(), 0L)
            );

            scoredList.add(scored);
        }

        // 3. Sortăm descrescător după scor, apoi după rating și id pentru stabilitate
        scoredList.sort((a, b) -> {
            int cmp = Double.compare(b.score(), a.score());
            if (cmp != 0) return cmp;
            int ratingCmp = Double.compare(b.accommodation().getRating(), a.accommodation().getRating());
            if (ratingCmp != 0) return ratingCmp;
            return a.accommodation().getId().compareTo(b.accommodation().getId());
        });

        // 4. Paginare în memorie
        long totalElements = scoredList.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, (int) totalElements);
        int toIndex = Math.min(fromIndex + size, (int) totalElements);

        List<AccommodationDto> pagedContent = scoredList.subList(fromIndex, toIndex).stream()
                .map(s -> AccommodationDto.from(s.accommodation(), s.score(), s.reason()))
                .toList();

        boolean isLast = page >= totalPages - 1;
        return new PageResponse<>(pagedContent, page, size, totalElements, totalPages, isLast);
    }

    /**
     * Construiește ponderile tag-urilor preferate de utilizator pe baza interacțiunilor sale.
     */
    public Map<String, Double> buildUserTagWeights(List<Booking> bookings, List<Favorite> favorites, List<ViewEvent> views) {
        Map<String, Double> tagWeights = new HashMap<>();

        for (Booking booking : bookings) {
            if (booking.getAccommodation() != null && booking.getAccommodation().getTags() != null) {
                for (String tag : booking.getAccommodation().getTags()) {
                    tagWeights.merge(tag.trim().toLowerCase(), 3.0, Double::sum);
                }
            }
        }

        for (Favorite favorite : favorites) {
            if (favorite.getAccommodation() != null && favorite.getAccommodation().getTags() != null) {
                for (String tag : favorite.getAccommodation().getTags()) {
                    tagWeights.merge(tag.trim().toLowerCase(), 2.0, Double::sum);
                }
            }
        }

        for (ViewEvent view : views) {
            if (view.getAccommodation() != null && view.getAccommodation().getTags() != null) {
                for (String tag : view.getAccommodation().getTags()) {
                    tagWeights.merge(tag.trim().toLowerCase(), 1.0, Double::sum);
                }
            }
        }

        return tagWeights;
    }

    /**
     * Calculează scorul și generează motivul recomandării pentru o cazare.
     */
    public ScoredAccommodation computeScore(
            Accommodation acc,
            UserPreference pref,
            Map<String, Double> userTagWeights,
            double maxUserTagWeight,
            Set<String> bookedRegions,
            Set<String> bookedCountries,
            Map<String, String> regionToCountryBooked,
            List<SimilarRegion> allSimilarRegions,
            boolean isFavorite,
            long viewCount
    ) {
        // 1. typeMatch (pondere 0.30)
        double typeMatch = 1.0;
        if (pref != null && pref.getPreferredTypes() != null && !pref.getPreferredTypes().isEmpty()) {
            typeMatch = pref.getPreferredTypes().contains(acc.getType()) ? 1.0 : 0.0;
        }

        // 2. budgetMatch (pondere 0.20)
        double budgetMatch = 1.0;
        if (pref != null && (pref.getBudgetMin() != null || pref.getBudgetMax() != null)) {
            double price = acc.getPricePerNight().doubleValue();
            double min = pref.getBudgetMin() != null ? pref.getBudgetMin().doubleValue() : 0.0;
            double max = pref.getBudgetMax() != null ? pref.getBudgetMax().doubleValue() : Double.MAX_VALUE;

            if (price >= min && price <= max) {
                budgetMatch = 1.0;
            } else if (price < min && min > 0) {
                budgetMatch = Math.max(0.0, 1.0 - (min - price) / min);
            } else if (price > max && max > 0) {
                budgetMatch = Math.max(0.0, 1.0 - (price - max) / max);
            }
        }

        // 3. allInclusiveMatch (pondere 0.10)
        double allInclusiveMatch = 1.0;
        if (pref != null && pref.getAllInclusivePreference() != null) {
            if (pref.getAllInclusivePreference() == AllInclusivePreference.YES) {
                allInclusiveMatch = acc.isAllInclusive() ? 1.0 : 0.0;
            } else if (pref.getAllInclusivePreference() == AllInclusivePreference.NO) {
                allInclusiveMatch = !acc.isAllInclusive() ? 1.0 : 0.0;
            }
        }

        // 4. tagAffinity (pondere 0.20)
        double tagAffinity = 0.0;
        List<String> accTags = acc.getTags() != null ? acc.getTags() : Collections.emptyList();
        if (!userTagWeights.isEmpty() && !accTags.isEmpty() && maxUserTagWeight > 0) {
            double sum = 0.0;
            for (String tag : accTags) {
                Double weight = userTagWeights.get(tag.trim().toLowerCase());
                if (weight != null) {
                    sum += (weight / maxUserTagWeight);
                }
            }
            tagAffinity = Math.min(1.0, sum / accTags.size());
        }

        // 5. regionDiversityBonus (pondere 0.15)
        // Bonus activ dacă regiunea este similară cu una în care utilizatorul a rezervat,
        // dar NU este aceeași regiune și NU este aceeași țară.
        double regionDiversityBonus = 0.0;
        String similarBookedRegion = null;
        String similarBookedCountry = null;

        boolean isSameRegion = bookedRegions.contains(acc.getRegion());
        boolean isSameCountry = bookedCountries.contains(acc.getCountry());

        if (!isSameRegion && !isSameCountry && !bookedRegions.isEmpty()) {
            for (SimilarRegion sr : allSimilarRegions) {
                String candidateA = sr.getRegionA();
                String candidateB = sr.getRegionB();

                if (candidateA.equalsIgnoreCase(acc.getRegion()) && bookedRegions.stream().anyMatch(candidateB::equalsIgnoreCase)) {
                    regionDiversityBonus = 1.0;
                    similarBookedRegion = candidateB;
                    similarBookedCountry = regionToCountryBooked.get(candidateB);
                    break;
                } else if (candidateB.equalsIgnoreCase(acc.getRegion()) && bookedRegions.stream().anyMatch(candidateA::equalsIgnoreCase)) {
                    regionDiversityBonus = 1.0;
                    similarBookedRegion = candidateA;
                    similarBookedCountry = regionToCountryBooked.get(candidateA);
                    break;
                }
            }
        }

        // 6. normalizedRating (pondere 0.05)
        double normalizedRating = Math.max(0.0, Math.min(1.0, acc.getRating() / 5.0));

        // 7. Penalizări:
        // -0.5 dacă utilizatorul a rezervat deja în aceeași regiune
        // -0.2 dacă utilizatorul a vizualizat cazarea de mai mult de 3 ori fără să o rezerve sau să o adauge la favorite
        double penalty = 0.0;
        if (isSameRegion) {
            penalty += 0.5;
        }
        if (viewCount > 3 && !isFavorite) {
            penalty += 0.2;
        }

        // Scor agregat conform formulei
        double rawScore = 0.30 * typeMatch
                + 0.20 * budgetMatch
                + 0.10 * allInclusiveMatch
                + 0.20 * tagAffinity
                + 0.15 * regionDiversityBonus
                + 0.05 * normalizedRating
                - penalty;

        double finalScore = Math.max(0.0, Math.round(rawScore * 100.0) / 100.0);

        // Generare motiv explicativ transparent în limba română
        String reason;
        if (regionDiversityBonus > 0 && similarBookedRegion != null) {
            String countryText = similarBookedCountry != null ? " din " + similarBookedCountry : "";
            reason = "Similar cu " + similarBookedRegion + countryText + ", dar intr-o tara noua (" + acc.getCountry() + ")";
        } else if (isFavorite) {
            reason = "Salvat la cazarile tale favorite";
        } else if (typeMatch > 0.5 && budgetMatch >= 0.8) {
            reason = "Se potriveste cu preferintele tale de " + acc.getType().name().toLowerCase() + " si bugetul ales";
        } else if (tagAffinity >= 0.4) {
            reason = "Recomandat pe baza activitatilor si tag-urilor preferate de tine";
        } else if (acc.getRating() >= 4.5) {
            reason = "Apreciat de alti oaspeti (rating excelent " + acc.getRating() + "/5)";
        } else {
            reason = "Recomandare personalizata StayFinder";
        }

        return new ScoredAccommodation(acc, finalScore, reason);
    }
}
