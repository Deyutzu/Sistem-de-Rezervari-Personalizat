package com.stayfinder.controller;

import com.stayfinder.dto.AccommodationDto;
import com.stayfinder.dto.PageResponse;
import com.stayfinder.model.AccommodationType;
import com.stayfinder.security.UserPrincipal;
import com.stayfinder.service.AccommodationService;
import com.stayfinder.service.AccommodationService.SearchCriteria;
import com.stayfinder.service.AccommodationService.SortOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Endpoint-uri publice (fără autentificare) pentru cazări.
 *
 * Validarea parametrilor (@Min, @PositiveOrZero...) se face automat de Spring 6.1+
 * înainte de a intra în metodă; dacă e încălcată, GlobalExceptionHandler
 * răspunde cu 400 și lista câmpurilor greșite.
 */
@RestController
@RequestMapping("/api/accommodations")
@Tag(name = "Cazări", description = "Căutare și detalii cazări (public)")
public class AccommodationController {

    private final AccommodationService accommodationService;

    public AccommodationController(AccommodationService accommodationService) {
        this.accommodationService = accommodationService;
    }

    @GetMapping
    @Operation(summary = "Listă de cazări cu filtre opționale și paginare")
    public PageResponse<AccommodationDto> list(
            @Parameter(description = "Tipul destinației") @RequestParam(required = false) AccommodationType type,
            @Parameter(description = "Preț minim / noapte (EUR)") @RequestParam(required = false) @PositiveOrZero BigDecimal minPrice,
            @Parameter(description = "Preț maxim / noapte (EUR)") @RequestParam(required = false) @PositiveOrZero BigDecimal maxPrice,
            @Parameter(description = "Doar all-inclusive (true) / doar fără (false)") @RequestParam(required = false) Boolean allInclusive,
            @Parameter(description = "Țara, ex. Italia") @RequestParam(required = false) String country,
            @Parameter(description = "Text liber: titlu, oraș, regiune, țară") @RequestParam(required = false) @Size(max = 100) String search,
            @Parameter(description = "Sortare") @RequestParam(defaultValue = "RATING") SortOption sort,
            @Parameter(description = "Numărul paginii (de la 0)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Elemente pe pagină (1-100)") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        SearchCriteria criteria = new SearchCriteria(type, minPrice, maxPrice, allInclusive, country, search);
        return accommodationService.search(criteria, page, size, sort);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detaliile unei cazări (înregistrează ViewEvent dacă utilizatorul este autentificat)")
    public AccommodationDto getById(@PathVariable Long id,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return accommodationService.getById(id, userId);
    }
}
