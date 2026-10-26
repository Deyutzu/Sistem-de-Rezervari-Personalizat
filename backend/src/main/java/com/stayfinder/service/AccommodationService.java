package com.stayfinder.service;

import com.stayfinder.dto.AccommodationDto;
import com.stayfinder.dto.PageResponse;
import com.stayfinder.exception.ResourceNotFoundException;
import com.stayfinder.model.Accommodation;
import com.stayfinder.model.AccommodationType;
import com.stayfinder.model.User;
import com.stayfinder.model.ViewEvent;
import com.stayfinder.repository.AccommodationRepository;
import com.stayfinder.repository.UserRepository;
import com.stayfinder.repository.ViewEventRepository;
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
    private final UserRepository userRepository;
    private final ViewEventRepository viewEventRepository;

    // Injectare prin constructor: dependențele sunt explicite și clasa e ușor de testat
    public AccommodationService(AccommodationRepository accommodationRepository,
                                UserRepository userRepository,
                                ViewEventRepository viewEventRepository) {
        this.accommodationRepository = accommodationRepository;
        this.userRepository = userRepository;
        this.viewEventRepository = viewEventRepository;
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

    @Transactional
    public AccommodationDto getById(Long id, Long currentUserId) {
        Accommodation accommodation = findEntity(id);

        // Dacă utilizatorul este autentificat, înregistrăm un ViewEvent pentru învățare din comportament
        if (currentUserId != null) {
            userRepository.findById(currentUserId).ifPresent(user -> {
                viewEventRepository.save(new ViewEvent(user, accommodation));
            });
        }

        return AccommodationDto.from(accommodation);
    }

    @Transactional
    public AccommodationDto getById(Long id) {
        return getById(id, null);
    }

    /** Folosită intern și de alte servicii (rezervări, favorite) în fazele următoare. */
    @Transactional(readOnly = true)
    public Accommodation findEntity(Long id) {
        return accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cazarea cu id " + id + " nu există"));
    }

    @Transactional
    public AccommodationDto create(com.stayfinder.dto.AccommodationRequest req) {
        Accommodation acc = new Accommodation();
        copyFromRequest(acc, req);
        Accommodation saved = accommodationRepository.save(acc);
        return AccommodationDto.from(saved);
    }

    @Transactional
    public AccommodationDto update(Long id, com.stayfinder.dto.AccommodationRequest req) {
        Accommodation acc = findEntity(id);
        copyFromRequest(acc, req);
        Accommodation saved = accommodationRepository.save(acc);
        return AccommodationDto.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Accommodation acc = findEntity(id);
        accommodationRepository.delete(acc);
    }

    private void copyFromRequest(Accommodation acc, com.stayfinder.dto.AccommodationRequest req) {
        acc.setTitle(req.title());
        acc.setDescription(req.description());
        acc.setCountry(req.country());
        acc.setRegion(req.region());
        acc.setCity(req.city());
        acc.setType(req.type());
        acc.setPricePerNight(req.pricePerNight());
        acc.setAllInclusive(req.allInclusive());
        acc.setRating(req.rating());
        acc.setImageUrl(req.imageUrl());
        acc.setLatitude(req.latitude());
        acc.setLongitude(req.longitude());
        if (req.tags() != null) {
            acc.setTags(new java.util.ArrayList<>(req.tags()));
        }
        acc.setSuitableFor(req.suitableFor());
    }
}
