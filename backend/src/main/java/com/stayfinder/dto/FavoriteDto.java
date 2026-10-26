package com.stayfinder.dto;

import com.stayfinder.model.Favorite;

import java.time.Instant;

public record FavoriteDto(
        Long id,
        AccommodationDto accommodation,
        Instant createdAt
) {
    public static FavoriteDto from(Favorite f) {
        return new FavoriteDto(
                f.getId(),
                AccommodationDto.from(f.getAccommodation()),
                f.getCreatedAt()
        );
    }
}
