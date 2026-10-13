package com.stayfinder.repository;

import com.stayfinder.model.Booking;
import com.stayfinder.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Folosit de recomandări: doar rezervările confirmate contează ca "am fost acolo"
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);
}
