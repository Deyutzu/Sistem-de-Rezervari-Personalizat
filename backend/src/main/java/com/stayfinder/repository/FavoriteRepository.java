package com.stayfinder.repository;

import com.stayfinder.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Favorite> findByUserIdAndAccommodationId(Long userId, Long accommodationId);

    boolean existsByUserIdAndAccommodationId(Long userId, Long accommodationId);
}
