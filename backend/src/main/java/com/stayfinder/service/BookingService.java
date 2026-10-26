package com.stayfinder.service;

import com.stayfinder.dto.BookingDto;
import com.stayfinder.dto.CreateBookingRequest;
import com.stayfinder.exception.BadRequestException;
import com.stayfinder.exception.ResourceNotFoundException;
import com.stayfinder.model.Accommodation;
import com.stayfinder.model.Booking;
import com.stayfinder.model.BookingStatus;
import com.stayfinder.model.User;
import com.stayfinder.repository.AccommodationRepository;
import com.stayfinder.repository.BookingRepository;
import com.stayfinder.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          AccommodationRepository accommodationRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.accommodationRepository = accommodationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(BookingDto::from)
                .toList();
    }

    @Transactional
    public BookingDto createBooking(Long userId, CreateBookingRequest req) {
        if (!req.checkOut().isAfter(req.checkIn())) {
            throw new BadRequestException("Data de check-out trebuie să fie strict după data de check-in");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul cu id " + userId + " nu există"));
        Accommodation accommodation = accommodationRepository.findById(req.accommodationId())
                .orElseThrow(() -> new ResourceNotFoundException("Cazarea cu id " + req.accommodationId() + " nu există"));

        long nights = ChronoUnit.DAYS.between(req.checkIn(), req.checkOut());
        if (nights < 1) {
            nights = 1;
        }

        BigDecimal totalPrice = accommodation.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = new Booking(
                user,
                accommodation,
                req.checkIn(),
                req.checkOut(),
                req.guests(),
                totalPrice
        );

        Booking saved = bookingRepository.save(booking);
        return BookingDto.from(saved);
    }

    @Transactional
    public BookingDto cancelBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervarea cu id " + bookingId + " nu există"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Nu poți anula o rezervare care aparține altui utilizator");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return BookingDto.from(booking);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);
        return BookingDto.from(updated);
    }
}
