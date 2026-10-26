package com.stayfinder.controller;

import com.stayfinder.dto.FavoriteDto;
import com.stayfinder.security.UserPrincipal;
import com.stayfinder.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/favorites")
@Tag(name = "Favorite", description = "Gestionare cazări favorite ale utilizatorului curent")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    @Operation(summary = "Lista cazărilor favorite ale utilizatorului curent")
    public List<FavoriteDto> listFavorites(@AuthenticationPrincipal UserPrincipal principal) {
        return favoriteService.getFavorites(principal.getId());
    }

    @PostMapping("/{accommodationId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adaugă o cazare la favorite")
    public FavoriteDto addFavorite(@AuthenticationPrincipal UserPrincipal principal,
                                   @PathVariable Long accommodationId) {
        return favoriteService.addFavorite(principal.getId(), accommodationId);
    }

    @DeleteMapping("/{accommodationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimină o cazare de la favorite")
    public void removeFavorite(@AuthenticationPrincipal UserPrincipal principal,
                               @PathVariable Long accommodationId) {
        favoriteService.removeFavorite(principal.getId(), accommodationId);
    }
}
