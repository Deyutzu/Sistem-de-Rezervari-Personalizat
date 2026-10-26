package com.stayfinder.service;

import com.stayfinder.dto.AccommodationDto;
import com.stayfinder.dto.PageResponse;
import com.stayfinder.model.*;
import com.stayfinder.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Teste unitare pentru RecommendationService (motorul de recomandare).
 * Verifică:
 * 1. Scenariul Dolomiți (Italia) -> Dolomiți Lienz (Austria) cu diversity bonus = 1.0 și motiv corect.
 * 2. Penalizarea de -0.5 pentru rezervare în aceeași regiune.
 * 3. Penalizarea de -0.2 pentru cazare vizualizată > 3 ori fără interacțiune.
 * 4. Excluderea completă din feed a cazărilor deja rezervate (CONFIRMED).
 * 5. Ponderile pentru tagAffinity, typeMatch, budgetMatch.
 */
@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ViewEventRepository viewEventRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private SimilarRegionRepository similarRegionRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private User testUser;
    private Accommodation dolomitiItalyAcc;
    private Accommodation lienzAustriaAcc;

    @BeforeEach
    void setUp() {
        testUser = new User("test@stayfinder.com", "hash", "Test User", Role.USER);
        setId(testUser, 10L);

        dolomitiItalyAcc = new Accommodation();
        setId(dolomitiItalyAcc, 101L);
        dolomitiItalyAcc.setTitle("Chalet Dolomiți Italia");
        dolomitiItalyAcc.setCountry("Italia");
        dolomitiItalyAcc.setRegion("Dolomiți");
        dolomitiItalyAcc.setCity("Cortina d'Ampezzo");
        dolomitiItalyAcc.setType(AccommodationType.MOUNTAIN);
        dolomitiItalyAcc.setPricePerNight(BigDecimal.valueOf(150.0));
        dolomitiItalyAcc.setRating(4.8);
        dolomitiItalyAcc.setTags(List.of("schi", "munte", "panoramic"));

        lienzAustriaAcc = new Accommodation();
        setId(lienzAustriaAcc, 102L);
        lienzAustriaAcc.setTitle("Alpine Lodge Lienz");
        lienzAustriaAcc.setCountry("Austria");
        lienzAustriaAcc.setRegion("Dolomiții Lienz");
        lienzAustriaAcc.setCity("Lienz");
        lienzAustriaAcc.setType(AccommodationType.MOUNTAIN);
        lienzAustriaAcc.setPricePerNight(BigDecimal.valueOf(140.0));
        lienzAustriaAcc.setRating(4.9);
        lienzAustriaAcc.setTags(List.of("schi", "munte", "spa"));
    }

    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void regionDiversityBonusAppliedWhenSimilarRegionInDifferentCountry() {
        // Scenariu: Utilizatorul are o rezervare CONFIRMATĂ în Dolomiți, Italia.
        // Sistemul conține relația de similaritate: (Dolomiți, Dolomiții Lienz, 0.95).
        Booking confirmedBooking = new Booking(testUser, dolomitiItalyAcc, LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), 2, BigDecimal.valueOf(750));
        SimilarRegion similarity = new SimilarRegion("Dolomiți", "Dolomiții Lienz", 0.95);

        Set<String> bookedRegions = Set.of("Dolomiți");
        Set<String> bookedCountries = Set.of("Italia");
        Map<String, String> regionToCountry = Map.of("Dolomiți", "Italia");
        List<SimilarRegion> similarities = List.of(similarity);

        // Cazarea din Austria (Dolomiții Lienz) este evaluată
        RecommendationService.ScoredAccommodation scored = recommendationService.computeScore(
                lienzAustriaAcc,
                null, // fără preferințe explicite
                Collections.emptyMap(),
                1.0,
                bookedRegions,
                bookedCountries,
                regionToCountry,
                similarities,
                false,
                0L
        );

        // Region diversity bonus este 1.0 (pondere 0.15)
        // Scorul trebuie să conțină bonusul de diversitate
        assertTrue(scored.score() >= 0.70);
        assertTrue(scored.reason().contains("Similar cu Dolomiți din Italia, dar intr-o tara noua (Austria)"));
    }

    @Test
    void penaltyAppliedWhenBookingInSameRegion() {
        // Dacă utilizatorul a rezervat deja în Dolomiți, o altă cazare din Dolomiți primește penalizare de -0.5
        Set<String> bookedRegions = Set.of("Dolomiți");
        Set<String> bookedCountries = Set.of("Italia");
        Map<String, String> regionToCountry = Map.of("Dolomiți", "Italia");

        RecommendationService.ScoredAccommodation scored = recommendationService.computeScore(
                dolomitiItalyAcc,
                null,
                Collections.emptyMap(),
                1.0,
                bookedRegions,
                bookedCountries,
                regionToCountry,
                Collections.emptyList(),
                false,
                0L
        );

        // Formula cu typeMatch=1 (0.3), budgetMatch=1 (0.2), allInclusiveMatch=1 (0.1), tagAffinity=0,
        // rating=4.8/5=0.96 * 0.05 = 0.048 => total brut = 0.648 - penalty(0.5) = 0.148 => rotunjit 0.15
        assertEquals(0.15, scored.score(), 0.02);
    }

    @Test
    void penaltyAppliedWhenViewedMoreThanThreeTimesWithoutInteraction() {
        // Vizualizat de 4 ori, dar fără favorite și fără rezervare => penalizare -0.2
        RecommendationService.ScoredAccommodation scored = recommendationService.computeScore(
                lienzAustriaAcc,
                null,
                Collections.emptyMap(),
                1.0,
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyMap(),
                Collections.emptyList(),
                false, // nu e favorit
                4L    // 4 vizualizări
        );

        // Brut: 0.30 + 0.20 + 0.10 + 0.049 - 0.20 = 0.449 => rotunjit 0.45
        assertEquals(0.45, scored.score(), 0.02);
    }

    @Test
    void alreadyBookedAccommodationsExcludedFromFeed() {
        // Cazarea 101 este rezervată confirmată
        Booking booking = new Booking(testUser, dolomitiItalyAcc, LocalDate.now().minusDays(20), LocalDate.now().minusDays(15), 2, BigDecimal.valueOf(750));

        when(userPreferenceRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(bookingRepository.findByUserIdAndStatus(10L, BookingStatus.CONFIRMED)).thenReturn(List.of(booking));
        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(Collections.emptyList());
        when(viewEventRepository.findByUserId(10L)).thenReturn(Collections.emptyList());
        when(similarRegionRepository.findAll()).thenReturn(List.of(new SimilarRegion("Dolomiți", "Dolomiții Lienz", 0.95)));
        when(accommodationRepository.findAll()).thenReturn(List.of(dolomitiItalyAcc, lienzAustriaAcc));

        PageResponse<AccommodationDto> feed = recommendationService.getPersonalizedFeed(10L, 0, 10);

        // dolomitiItalyAcc trebuie să fie complet exclusă
        assertEquals(1, feed.totalElements());
        assertEquals(102L, feed.content().get(0).id());
        assertEquals("Alpine Lodge Lienz", feed.content().get(0).title());
    }

    @Test
    void tagAffinityCalculatedFromInteractions() {
        Booking booking = new Booking(testUser, dolomitiItalyAcc, LocalDate.now().minusDays(5), LocalDate.now().minusDays(2), 2, BigDecimal.valueOf(400));
        // dolomitiItalyAcc are tag-urile: "schi", "munte", "panoramic" (greutate 3 fiecare)

        Map<String, Double> tagWeights = recommendationService.buildUserTagWeights(
                List.of(booking),
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertEquals(3.0, tagWeights.get("schi"));
        assertEquals(3.0, tagWeights.get("munte"));
        assertEquals(3.0, tagWeights.get("panoramic"));
    }
}
