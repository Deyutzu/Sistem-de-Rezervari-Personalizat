package com.stayfinder.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Un eveniment "utilizatorul a deschis detaliul unei cazări".
 * De ce îl salvăm: algoritmul de recomandare învață din comportament
 * (tag-urile cazărilor văzute) și penalizează cazările văzute de multe ori
 * fără nicio interacțiune (favorite/rezervare).
 */
@Entity
@Table(name = "view_events", indexes = @Index(name = "idx_view_user", columnList = "user_id"))
public class ViewEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Accommodation accommodation;

    @Column(nullable = false)
    private Instant viewedAt = Instant.now();

    protected ViewEvent() {
    }

    public ViewEvent(User user, Accommodation accommodation) {
        this.user = user;
        this.accommodation = accommodation;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Accommodation getAccommodation() { return accommodation; }
    public Instant getViewedAt() { return viewedAt; }
}
