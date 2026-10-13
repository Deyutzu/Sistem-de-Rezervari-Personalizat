package com.stayfinder.service;

import com.stayfinder.dto.AccommodationDto;
import com.stayfinder.dto.PageResponse;
import com.stayfinder.exception.ResourceNotFoundException;
import com.stayfinder.model.Accommodation;
import com.stayfinder.model.AccommodationType;
import com.stayfinder.repository.AccommodationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.stayfinder.repository.AccommodationSpecifications.*;

/**
 * Logica de business pentru cazări.
 *
 * De ce un strat "service" separat de controller: controllerul se ocupă doar
 * de HTTP (parametri, coduri de răspuns), iar serviciul de reguli și acces la date.
 * Același serviciu va fi refolosit de interfața web Thymeleaf (faza 6).
 */
@Service
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;

    // Injectare prin constructor: dependențele sunt explicite și clasa e ușor de testat
    public AccommodationService(AccommodationRepository accommodationRepository) {
        this.accommodationRepository = accommodationRepository;
    }

    /** Criteriile de căutare grupate într-un singur obiect, ca să nu avem metode cu 9 parametri. */
    public record SearchCriteria(
            AccommodationType type,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean allInclusive,
            String country,
            String search
    ) {
    }

    /** Sortările permise. Le limităm explicit ca clientul să nu poată sorta după orice coloană. */
    public enum SortOption {
        RATING(Sort.by(Sort.Direction.DESC, "rating")),
        PRICE_ASC(Sort.by(Sort.Direction.ASC, "pricePerNight")),
        PRICE_DESC(Sort.by(Sort.Direction.DESC, "pricePerNight"));

        private final Sort sort;

        SortOption(Sort sort) {
            this.sort = sort;
        }
    }

    /**
     * readOnly = true: Hibernate nu mai verifică modificări la final (mai rapid)
     * și tranzacția rămâne deschisă cât timp convertim în DTO (tags e LAZY).
     */
    @Transactional(readOnly = true)
    public PageResponse<AccommodationDto> search(SearchCriteria c, int page, int size, SortOption sortOption) {
        // Combinăm doar filtrele trimise; cele null sunt ignorate automat.
        Specification<Accommodation> spec = Specification.allOf(
                hasType(c.type()),
                priceAtLeast(c.minPrice()),
                priceAtMost(c.maxPrice()),
                isAllInclusive(c.allInclusive()),
                inCountry(c.country()),
                matchesSearch(c.search())
        );

        // "id" ca al doilea criteriu: ordine stabilă între pagini la valori egale
        Sort sort = sortOption.sort.and(Sort.by("id"));
        Page<Accommodation> result = accommodationRepository.findAll(spec, PageRequest.of(page, size, sort));
        return PageResponse.of(result, AccommodationDto::from);
    }

    @Transactional(readOnly = true)
    public AccommodationDto getById(Long id) {
        // TODO Faza 4: dacă cererea vine de la un utilizator autentificat,
        // salvăm aici și un ViewEvent (învățare din comportament).
        return AccommodationDto.from(findEntity(id));
    }

    /** Folosită intern și de alte servicii (rezervări, favorite) în fazele următoare. */
    @Transactional(readOnly = true)
    public Accommodation findEntity(Long id) {
        return accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cazarea cu id " + id + " nu există"));
    }
}
