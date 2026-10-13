package com.stayfinder.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de integrare: pornește aplicația completă (cu H2 + importul JSON)
 * și face cereri HTTP simulate cu MockMvc. Verifică împreună controllerul,
 * serviciul, filtrele JPA, importul de date și handlerul de erori.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AccommodationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listReturnsSeededDataPaginated() throws Exception {
        mockMvc.perform(get("/api/accommodations").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(60))
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalPages").value(6));
    }

    @Test
    void filtersByTypeAndCountry() throws Exception {
        mockMvc.perform(get("/api/accommodations")
                        .param("type", "MOUNTAIN")
                        .param("country", "austria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.content[*].country", everyItem(is("Austria"))))
                .andExpect(jsonPath("$.content[*].type", everyItem(is("MOUNTAIN"))));
    }

    @Test
    void filtersByPriceAndAllInclusiveSortedByPrice() throws Exception {
        mockMvc.perform(get("/api/accommodations")
                        .param("maxPrice", "100")
                        .param("allInclusive", "true")
                        .param("sort", "PRICE_ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].allInclusive", everyItem(is(true))))
                .andExpect(jsonPath("$.content[*].pricePerNight", everyItem(lessThanOrEqualTo(100.0))))
                .andExpect(jsonPath("$.content[0].city").value("Eforie Nord"));
    }

    @Test
    void searchMatchesRegion() throws Exception {
        mockMvc.perform(get("/api/accommodations").param("search", "dolomi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(8)); // 6 Italia + 2 Lienz
    }

    @Test
    void detailReturnsTags() throws Exception {
        mockMvc.perform(get("/api/accommodations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tags", not(empty())));
    }

    @Test
    void unknownIdReturnsUniform404() throws Exception {
        mockMvc.perform(get("/api/accommodations/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/accommodations/99999"))
                .andExpect(jsonPath("$.message", containsString("99999")));
    }

    @Test
    void invalidParamsReturn400WithFieldErrors() throws Exception {
        mockMvc.perform(get("/api/accommodations").param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.size").exists());

        mockMvc.perform(get("/api/accommodations").param("type", "PLAJA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.type").exists());
    }
}
