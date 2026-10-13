package com.stayfinder.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * O cazare salvată la favorite. Constrângerea unică (user, accommodation)
 * împiedică salvarea aceleiași cazări de două ori direct la nivel de bază de date.
 */
@Entity
@Table(name = "favorites",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "accommodation_id"}))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Accommodation accommodation;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Favorite() {
    }

    public Favorite(User user, Accommodation accommodation) {
        this.user = user;
        this.accommodation = accommodation;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Accommodation getAccommodation() { return accommodation; }
    public Instant getCreatedAt() { return createdAt; }
}
