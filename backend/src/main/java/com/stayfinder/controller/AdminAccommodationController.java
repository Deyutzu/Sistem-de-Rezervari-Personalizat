package com.stayfinder.controller;

import com.stayfinder.dto.AccommodationDto;
import com.stayfinder.dto.AccommodationRequest;
import com.stayfinder.service.AccommodationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/accommodations")
@Tag(name = "Admin Cazări", description = "Operațiuni CRUD destinate exclusiv administratorilor (rol ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminAccommodationController {

    private final AccommodationService accommodationService;

    public AdminAccommodationController(AccommodationService accommodationService) {
        this.accommodationService = accommodationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adaugă o cazare nouă")
    public AccommodationDto create(@Valid @RequestBody AccommodationRequest request) {
        return accommodationService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizează o cazare existentă")
    public AccommodationDto update(@PathVariable Long id, @Valid @RequestBody AccommodationRequest request) {
        return accommodationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Șterge o cazare")
    public void delete(@PathVariable Long id) {
        accommodationService.delete(id);
    }
}
