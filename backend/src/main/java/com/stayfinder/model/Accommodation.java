package com.stayfinder.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * O cazare (hotel, pensiune, apartament).
 *
 * De ce "country" și "region" sunt câmpuri separate: algoritmul de recomandare
 * (faza 4) compară regiunile între ele (tabela SimilarRegion) și verifică dacă
 * o cazare e în altă țară decât rezervările anterioare.
 */
@Entity
@Table(name = "accommodations", indexes = {
        @Index(name = "idx_acc_type", columnList = "type"),
        @Index(name = "idx_acc_region", columnList = "region")
})
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // length mare: descrierile pot depăși limita implicită de 255 caractere
    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccommodationType type;

    // BigDecimal pentru bani: double are erori de rotunjire (0.1 + 0.2 != 0.3)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    private boolean allInclusive;

    // Rating mediu 0-5 (ex. 4.6)
    private double rating;

    @Column(length = 500)
    private String imageUrl;

    private double latitude;
    private double longitude;

    /**
     * Tag-uri libere ("schi", "spa", "plajă").
     * @ElementCollection = o listă de valori simple stocată într-o tabelă separată
     * (accommodation_tags), fără să fie nevoie de o entitate "Tag" proprie.
     * LAZY: se încarcă doar când e nevoie (vezi default_batch_fetch_size în properties).
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "accommodation_tags", joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private TravelType suitableFor;

    public Accommodation() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public AccommodationType getType() { return type; }
    public void setType(AccommodationType type) { this.type = type; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
    public boolean isAllInclusive() { return allInclusive; }
    public void setAllInclusive(boolean allInclusive) { this.allInclusive = allInclusive; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public TravelType getSuitableFor() { return suitableFor; }
    public void setSuitableFor(TravelType suitableFor) { this.suitableFor = suitableFor; }
}
