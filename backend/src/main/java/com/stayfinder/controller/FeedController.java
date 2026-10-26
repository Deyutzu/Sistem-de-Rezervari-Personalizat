package com.stayfinder.controller;

import com.stayfinder.dto.AccommodationDto;
import com.stayfinder.dto.PageResponse;
import com.stayfinder.security.UserPrincipal;
import com.stayfinder.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
@Tag(name = "Feed", description = "Feed personalizat generat de motorul de recomandare (necesită autentificare)")
@SecurityRequirement(name = "bearerAuth")
public class FeedController {

    private final RecommendationService recommendationService;

    public FeedController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    @Operation(summary = "Obține feed-ul de cazări personalizate pentru utilizatorul curent")
    public PageResponse<AccommodationDto> getFeed(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Numărul paginii (începând de la 0)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Număr de elemente per pagină (1-100)") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return recommendationService.getPersonalizedFeed(principal.getId(), page, size);
    }
}
