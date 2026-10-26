package com.stayfinder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stayfinder.dto.AccommodationRequest;
import com.stayfinder.dto.CreateBookingRequest;
import com.stayfinder.dto.LoginRequest;
import com.stayfinder.dto.UserPreferenceDto;
import com.stayfinder.model.AccommodationType;
import com.stayfinder.model.AllInclusivePreference;
import com.stayfinder.model.TravelType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndFeedIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("token").asText();
    }

    @Test
    void authEndpointsAllowRegistrationAndLogin() throws Exception {
        // Autentificare cu utilizatorul pre-populat din DataSeeder
        String userToken = loginAndGetToken("user@stayfinder.com", "user123");
        org.junit.jupiter.api.Assertions.assertNotNull(userToken);
        org.junit.jupiter.api.Assertions.assertFalse(userToken.isBlank());

        // Autentificare cu parolă greșită -> 401
        LoginRequest badLogin = new LoginRequest("user@stayfinder.com", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void feedRequiresAuthenticationAndReturnsScoredAccommodations() throws Exception {
        // Fără token -> 401
        mockMvc.perform(get("/api/feed"))
                .andExpect(status().isUnauthorized());

        // Cu token de utilizator -> 200 OK și include scor + motiv
        String token = loginAndGetToken("user@stayfinder.com", "user123");
        mockMvc.perform(get("/api/feed")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].score").isNumber())
                .andExpect(jsonPath("$.content[0].reason").isString());
    }

    @Test
    void userPreferencesLifecycle() throws Exception {
        String token = loginAndGetToken("user@stayfinder.com", "user123");

        UserPreferenceDto dto = new UserPreferenceDto(
                Set.of(AccommodationType.MOUNTAIN),
                BigDecimal.valueOf(80.0),
                BigDecimal.valueOf(250.0),
                AllInclusivePreference.ANY,
                TravelType.COUPLE
        );

        mockMvc.perform(put("/api/me/preferences")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredTypes[0]").value("MOUNTAIN"))
                .andExpect(jsonPath("$.budgetMin").value(80.0))
                .andExpect(jsonPath("$.budgetMax").value(250.0))
                .andExpect(jsonPath("$.travelType").value("COUPLE"));

        mockMvc.perform(get("/api/me/preferences")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredTypes[0]").value("MOUNTAIN"));
    }

    @Test
    void favoritesLifecycle() throws Exception {
        String token = loginAndGetToken("user@stayfinder.com", "user123");

        // Adăugare la favorite
        mockMvc.perform(post("/api/me/favorites/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accommodation.id").value(2));

        // Listare favorite
        mockMvc.perform(get("/api/me/favorites")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].accommodation.id", hasItem(2)));

        // Ștergere din favorite
        mockMvc.perform(delete("/api/me/favorites/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void bookingCreationAndCancellation() throws Exception {
        String token = loginAndGetToken("user@stayfinder.com", "user123");

        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = LocalDate.now().plusDays(15);
        CreateBookingRequest request = new CreateBookingRequest(3L, checkIn, checkOut, 2);

        MvcResult result = mockMvc.perform(post("/api/me/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accommodation.id").value(3))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalPrice").isNumber())
                .andReturn();

        Long bookingId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Anulare rezervare
        mockMvc.perform(put("/api/me/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void roleBasedAccessControlForAdminEndpoints() throws Exception {
        String userToken = loginAndGetToken("user@stayfinder.com", "user123");
        String adminToken = loginAndGetToken("admin@stayfinder.com", "admin123");

        AccommodationRequest newAcc = new AccommodationRequest(
                "Boutique Hotel Test",
                "Descriere test admin",
                "România",
                "Transilvania",
                "Brașov",
                AccommodationType.MOUNTAIN,
                BigDecimal.valueOf(110.0),
                false,
                4.7,
                "https://images.unsplash.com/photo-test",
                45.65,
                25.60,
                List.of("munte", "centru"),
                TravelType.FAMILY
        );

        // Utilizator obișnuit -> 403 Forbidden
        mockMvc.perform(post("/api/admin/accommodations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAcc)))
                .andExpect(status().isForbidden());

        // Administrator -> 201 Created
        mockMvc.perform(post("/api/admin/accommodations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAcc)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Boutique Hotel Test"));
    }
}
