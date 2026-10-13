package com.stayfinder.model;

import jakarta.persistence.*;

/**
 * Leagă două regiuni "asemănătoare" (ex. "Dolomiti" din Italia și "Dolomiții Lienz" din Austria).
 *
 * De ce un tabel și nu cod: relațiile de similaritate sunt DATE, nu logică.
 * Le putem modifica fără recompilare, iar algoritmul rămâne generic.
 * Relația este simetrică: perechea (A, B) se stochează o singură dată,
 * iar repository-ul caută în ambele sensuri.
 */
@Entity
@Table(name = "similar_regions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"region_a", "region_b"}))
public class SimilarRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region_a", nullable = false)
    private String regionA;

    @Column(name = "region_b", nullable = false)
    private String regionB;

    // 0..1: cât de asemănătoare sunt regiunile (1 = practic identice ca experiență)
    private double similarityWeight;

    protected SimilarRegion() {
    }

    public SimilarRegion(String regionA, String regionB, double similarityWeight) {
        this.regionA = regionA;
        this.regionB = regionB;
        this.similarityWeight = similarityWeight;
    }

    public Long getId() { return id; }
    public String getRegionA() { return regionA; }
    public String getRegionB() { return regionB; }
    public double getSimilarityWeight() { return similarityWeight; }
}
