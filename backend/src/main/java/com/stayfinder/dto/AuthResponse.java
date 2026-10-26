package com.stayfinder.dto;

import com.stayfinder.model.Role;

public record AuthResponse(
        String token,
        Long id,
        String email,
        String displayName,
        Role role
) {
}
