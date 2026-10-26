package com.stayfinder.security;

import com.stayfinder.model.Role;
import com.stayfinder.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adaptor între entitatea noastră User și interfața UserDetails cerută de Spring Security.
 * Păstrează id-ul și rolul utilizatorului pentru acces facil în servicii și controllere.
 */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String displayName;
    private final Role role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String passwordHash, String displayName, Role role) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
        // Prefixul "ROLE_" este convenția standard Spring Security pentru roluri
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public static UserPrincipal fromUser(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getDisplayName(),
                user.getRole()
        );
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
