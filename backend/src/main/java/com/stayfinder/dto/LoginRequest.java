package com.stayfinder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email-ul este obligatoriu")
        @Email(message = "Formatul adresei de email este invalid")
        String email,

        @NotBlank(message = "Parola este obligatorie")
        String password
) {
}
