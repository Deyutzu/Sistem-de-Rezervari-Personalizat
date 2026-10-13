package com.stayfinder.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Preferințele alese la onboarding. Relație 1-la-1 cu User:
 * fiecare utilizator are cel mult un set de preferințe.
 */
@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(unique = true)
    private User user;

    // Un utilizator poate prefera mai multe tipuri (ex. mare ȘI munte)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_types", joinColumns = @JoinColumn(name = "preference_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Set<AccommodationType> preferredTypes = new HashSet<>();

    @Column(precision = 10, scale = 2)
    private BigDecimal budgetMin;

    @Column(precision = 10, scale = 2)
    private BigDecimal budgetMax;

    @Enumerated(EnumType.STRING)
    private AllInclusivePreference allInclusivePreference = AllInclusivePreference.ANY;

    @Enumerated(EnumType.STRING)
    private TravelType travelType;

    protected UserPreference() {
    }

    public UserPreference(User user) {
        this.user = user;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Set<AccommodationType> getPreferredTypes() { return preferredTypes; }
    public void setPreferredTypes(Set<AccommodationType> preferredTypes) { this.preferredTypes = preferredTypes; }
    public BigDecimal getBudgetMin() { return budgetMin; }
    public void setBudgetMin(BigDecimal budgetMin) { this.budgetMin = budgetMin; }
    public BigDecimal getBudgetMax() { return budgetMax; }
    public void setBudgetMax(BigDecimal budgetMax) { this.budgetMax = budgetMax; }
    public AllInclusivePreference getAllInclusivePreference() { return allInclusivePreference; }
    public void setAllInclusivePreference(AllInclusivePreference p) { this.allInclusivePreference = p; }
    public TravelType getTravelType() { return travelType; }
    public void setTravelType(TravelType travelType) { this.travelType = travelType; }
}
