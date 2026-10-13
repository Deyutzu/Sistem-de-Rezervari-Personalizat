package com.stayfinder.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * O rezervare. Plata este SIMULATĂ: la creare rezervarea devine direct CONFIRMED.
 * totalPrice se calculează pe server (nopți * preț/noapte), nu se primește de la client,
 * ca să nu poată fi manipulat.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LAZY: nu încărcăm utilizatorul/cazarea decât dacă le accesăm efectiv
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Accommodation accommodation;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    private int guests;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Booking() {
    }

    public Booking(User user, Accommodation accommodation, LocalDate checkIn, LocalDate checkOut,
                   int guests, BigDecimal totalPrice) {
        this.user = user;
        this.accommodation = accommodation;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guests = guests;
        this.totalPrice = totalPrice;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Accommodation getAccommodation() { return accommodation; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public int getGuests() { return guests; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
