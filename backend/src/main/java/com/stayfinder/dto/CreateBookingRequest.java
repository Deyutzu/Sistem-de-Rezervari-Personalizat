package com.stayfinder.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateBookingRequest(
        @NotNull(message = "Id-ul cazării este obligatoriu")
        Long accommodationId,

        @NotNull(message = "Data de check-in este obligatorie")
        @FutureOrPresent(message = "Data de check-in nu poate fi în trecut")
        LocalDate checkIn,

        @NotNull(message = "Data de check-out este obligatorie")
        LocalDate checkOut,

        @Min(value = 1, message = "Numărul de oaspeți trebuie să fie de cel puțin 1")
        int guests
) {
}
