package com.stayfinder.model;

import jakarta.persistence.*;

import java.time.Instant;

/** Recenzia unui utilizator pentru o cazare (funcționalitate opțională, faza 4+). */
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Accommodation accommodation;

    // 1-5 stele; validarea intervalului se face în DTO
    private int rating;

    @Column(length = 2000)
    private String text;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Review() {
    }

    public Review(User user, Accommodation accommodation, int rating, String text) {
        this.user = user;
        this.accommodation = accommodation;
        this.rating = rating;
        this.text = text;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Accommodation getAccommodation() { return accommodation; }
    public int getRating() { return rating; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }
}
