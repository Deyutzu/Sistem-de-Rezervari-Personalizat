package com.stayfinder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email-ul este obligatoriu")
        @Email(message = "Formatul adresei de email este invalid")
        String email,

        @NotBlank(message = "Parola este obligatorie")
        @Size(min = 6, message = "Parola trebuie să aibă cel puțin 6 caractere")
        String password,

        @NotBlank(message = "Numele afișat este obligatoriu")
        @Size(min = 2, max = 100, message = "Numele afișat trebuie să aibă între 2 și 100 caractere")
        String displayName
) {
}
