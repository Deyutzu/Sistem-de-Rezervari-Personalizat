package com.stayfinder.controller;

import com.stayfinder.dto.NearbyPlaceDto;
import com.stayfinder.external.OpenTripMapClient;
import com.stayfinder.model.Accommodation;
import com.stayfinder.service.AccommodationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint pentru obiective turistice în apropierea unei cazări (OpenTripMap).
 * Dacă cheia OPENTRIPMAP_API_KEY nu este setată, răspunde cu o listă goală.
 */
@RestController
@RequestMapping("/api/accommodations/{id}/nearby")
@Tag(name = "Obiective Turistice", description = "Atracții turistice din apropierea cazării (OpenTripMap)")
public class NearbyPlaceController {

    private final AccommodationService accommodationService;
    private final OpenTripMapClient openTripMapClient;

    public NearbyPlaceController(AccommodationService accommodationService, OpenTripMapClient openTripMapClient) {
        this.accommodationService = accommodationService;
        this.openTripMapClient = openTripMapClient;
    }

    @GetMapping
    @Operation(summary = "Obiective turistice pe o rază de 5 km în jurul cazării")
    public List<NearbyPlaceDto> getNearby(
            @Parameter(description = "ID-ul cazării") @PathVariable Long id
    ) {
        Accommodation accommodation = accommodationService.findEntity(id);
        return openTripMapClient.getNearbyPlaces(accommodation.getLatitude(), accommodation.getLongitude());
    }
}
