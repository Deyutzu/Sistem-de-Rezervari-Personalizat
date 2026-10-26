package com.stayfinder.controller;

import com.stayfinder.dto.BookingDto;
import com.stayfinder.dto.CreateBookingRequest;
import com.stayfinder.security.UserPrincipal;
import com.stayfinder.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/bookings")
@Tag(name = "Rezervări", description = "Gestionare rezervări ale utilizatorului curent (plată simulată)")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @Operation(summary = "Lista rezervărilor utilizatorului curent")
    public List<BookingDto> listBookings(@AuthenticationPrincipal UserPrincipal principal) {
        return bookingService.getUserBookings(principal.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Creează o rezervare nouă")
    public BookingDto createBooking(@AuthenticationPrincipal UserPrincipal principal,
                                   @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(principal.getId(), request);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Anulează o rezervare existentă")
    public BookingDto cancelBooking(@AuthenticationPrincipal UserPrincipal principal,
                                   @PathVariable Long id) {
        return bookingService.cancelBooking(principal.getId(), id);
    }
}
