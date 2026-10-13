package com.stayfinder.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Utilizatorul aplicației.
 * Tabela se numește "users" deoarece "user" este cuvânt rezervat în PostgreSQL și H2.
 * Parola NU se stochează niciodată în clar: doar hash-ul BCrypt (generat în faza 4).
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // EnumType.STRING salvează "USER"/"ADMIN" (lizibil și sigur la reordonarea enum-ului),
    // spre deosebire de ORDINAL care ar salva 0/1.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    // JPA are nevoie de un constructor fără argumente.
    protected User() {
    }

    public User(String email, String passwordHash, String displayName, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Instant getCreatedAt() { return createdAt; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
