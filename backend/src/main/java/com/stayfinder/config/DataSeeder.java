package com.stayfinder.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stayfinder.model.Accommodation;
import com.stayfinder.model.AccommodationType;
import com.stayfinder.model.SimilarRegion;
import com.stayfinder.model.TravelType;
import com.stayfinder.repository.AccommodationRepository;
import com.stayfinder.repository.SimilarRegionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Importă datele de test la pornirea aplicației.
 *
 * CommandLineRunner = cod rulat automat O DATĂ, după ce contextul Spring e gata
 * (deci repository-urile funcționează deja).
 * Importăm DOAR dacă tabela e goală, ca să nu dublăm datele la fiecare repornire
 * când folosim o bază persistentă (PostgreSQL).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AccommodationRepository accommodationRepository;
    private final SimilarRegionRepository similarRegionRepository;
    private final ObjectMapper objectMapper; // ObjectMapper-ul configurat de Spring Boot

    public DataSeeder(AccommodationRepository accommodationRepository,
                      SimilarRegionRepository similarRegionRepository,
                      ObjectMapper objectMapper) {
        this.accommodationRepository = accommodationRepository;
        this.similarRegionRepository = similarRegionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Forma unei cazări în fișierul JSON. Separată de entitate ca fișierul
     * să nu depindă de detaliile JPA (id generat, relații etc.).
     */
    record AccommodationSeed(String title, String description, String country, String region, String city,
                             AccommodationType type, BigDecimal pricePerNight, boolean allInclusive,
                             double rating, String imageUrl, double latitude, double longitude,
                             List<String> tags, TravelType suitableFor) {
    }

    record SimilarRegionSeed(String regionA, String regionB, double similarityWeight) {
    }

    @Override
    @Transactional // totul sau nimic: dacă un rând e invalid, nu rămâne un import pe jumătate
    public void run(String... args) throws IOException {
        if (accommodationRepository.count() == 0) {
            List<AccommodationSeed> seeds = readJson("data/accommodations.json", new TypeReference<>() {});
            accommodationRepository.saveAll(seeds.stream().map(DataSeeder::toEntity).toList());
            log.info("Import: {} cazări din accommodations.json", seeds.size());
        } else {
            log.info("Cazările există deja în baza de date, importul este omis");
        }

        if (similarRegionRepository.count() == 0) {
            List<SimilarRegionSeed> seeds = readJson("data/similar-regions.json", new TypeReference<>() {});
            similarRegionRepository.saveAll(seeds.stream()
                    .map(s -> new SimilarRegion(s.regionA(), s.regionB(), s.similarityWeight()))
                    .toList());
            log.info("Import: {} perechi de regiuni similare", seeds.size());
        }
    }

    private <T> T readJson(String path, TypeReference<T> type) throws IOException {
        // ClassPathResource citește din src/main/resources (inclusiv din JAR-ul final)
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(in, type);
        }
    }

    private static Accommodation toEntity(AccommodationSeed s) {
        Accommodation a = new Accommodation();
        a.setTitle(s.title());
        a.setDescription(s.description());
        a.setCountry(s.country());
        a.setRegion(s.region());
        a.setCity(s.city());
        a.setType(s.type());
        a.setPricePerNight(s.pricePerNight());
        a.setAllInclusive(s.allInclusive());
        a.setRating(s.rating());
        a.setImageUrl(s.imageUrl());
        a.setLatitude(s.latitude());
        a.setLongitude(s.longitude());
        a.setTags(s.tags() == null ? new ArrayList<>() : new ArrayList<>(s.tags()));
        a.setSuitableFor(s.suitableFor());
        return a;
    }
}
