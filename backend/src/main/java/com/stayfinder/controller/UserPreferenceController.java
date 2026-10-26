package com.stayfinder.controller;

import com.stayfinder.dto.UserPreferenceDto;
import com.stayfinder.security.UserPrincipal;
import com.stayfinder.service.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/preferences")
@Tag(name = "Preferințe", description = "Gestionare preferințe de călătorie ale utilizatorului autentificat")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    public UserPreferenceController(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @GetMapping
    @Operation(summary = "Obține preferințele utilizatorului curent")
    public UserPreferenceDto getPreferences(@AuthenticationPrincipal UserPrincipal principal) {
        return userPreferenceService.getPreferences(principal.getId());
    }

    @PutMapping
    @Operation(summary = "Actualizează preferințele utilizatorului curent")
    public UserPreferenceDto updatePreferences(@AuthenticationPrincipal UserPrincipal principal,
                                               @Valid @RequestBody UserPreferenceDto dto) {
        return userPreferenceService.saveOrUpdatePreferences(principal.getId(), dto);
    }
}
