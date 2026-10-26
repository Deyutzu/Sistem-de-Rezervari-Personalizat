package com.stayfinder.dto;

import com.stayfinder.model.Booking;
import com.stayfinder.model.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BookingDto(
        Long id,
        AccommodationDto accommodation,
        LocalDate checkIn,
        LocalDate checkOut,
        int guests,
        BigDecimal totalPrice,
        BookingStatus status,
        Instant createdAt
) {
    public static BookingDto from(Booking b) {
        return new BookingDto(
                b.getId(),
                AccommodationDto.from(b.getAccommodation()),
                b.getCheckIn(),
                b.getCheckOut(),
                b.getGuests(),
                b.getTotalPrice(),
                b.getStatus(),
                b.getCreatedAt()
        );
    }
}
