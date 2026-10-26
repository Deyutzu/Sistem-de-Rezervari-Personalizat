package com.stayfinder.service;

import com.stayfinder.dto.FavoriteDto;
import com.stayfinder.exception.ResourceNotFoundException;
import com.stayfinder.model.Accommodation;
import com.stayfinder.model.Favorite;
import com.stayfinder.model.User;
import com.stayfinder.repository.AccommodationRepository;
import com.stayfinder.repository.FavoriteRepository;
import com.stayfinder.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           AccommodationRepository accommodationRepository,
                           UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.accommodationRepository = accommodationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<FavoriteDto> getFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(FavoriteDto::from)
                .toList();
    }

    @Transactional
    public FavoriteDto addFavorite(Long userId, Long accommodationId) {
        return favoriteRepository.findByUserIdAndAccommodationId(userId, accommodationId)
                .map(FavoriteDto::from)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul cu id " + userId + " nu există"));
                    Accommodation accommodation = accommodationRepository.findById(accommodationId)
                            .orElseThrow(() -> new ResourceNotFoundException("Cazarea cu id " + accommodationId + " nu există"));

                    Favorite favorite = new Favorite(user, accommodation);
                    Favorite saved = favoriteRepository.save(favorite);
                    return FavoriteDto.from(saved);
                });
    }

    @Transactional
    public void removeFavorite(Long userId, Long accommodationId) {
        favoriteRepository.deleteByUserIdAndAccommodationId(userId, accommodationId);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long accommodationId) {
        return favoriteRepository.existsByUserIdAndAccommodationId(userId, accommodationId);
    }
}
