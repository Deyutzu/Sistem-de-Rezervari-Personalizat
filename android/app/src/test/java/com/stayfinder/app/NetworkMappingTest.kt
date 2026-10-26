package com.stayfinder.app

import com.stayfinder.app.data.network.*
import com.stayfinder.app.model.AccommodationType
import com.stayfinder.app.model.AllInclusivePreference
import com.stayfinder.app.model.TravelType
import com.stayfinder.app.model.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste unitare pentru maparea DTO-urilor de rețea Retrofit către modelele de domeniu UI.
 */
class NetworkMappingTest {

    @Test
    fun accommodationDtoMapsFieldsCorrectly() {
        val dto = NetworkAccommodationDto(
            id = 42L,
            title = "Grand Hotel Kronenhof",
            description = "Hotel istoric de lux în Alpi",
            country = "Elveția",
            region = "Graubünden",
            city = "Pontresina",
            type = "MOUNTAIN",
            pricePerNight = 320.0,
            allInclusive = false,
            rating = 4.9,
            imageUrl = "https://images.unsplash.com/test",
            latitude = 46.49,
            longitude = 9.87,
            tags = listOf("schi", "spa", "lux"),
            suitableFor = "COUPLE",
            score = 0.94,
            reason = "Recomandat pentru preferințele tale de munte"
        )

        val model = dto.toModel()

        assertEquals(42L, model.id)
        assertEquals("Grand Hotel Kronenhof", model.title)
        assertEquals(AccommodationType.MOUNTAIN, model.type)
        assertEquals(320.0, model.pricePerNight, 0.001)
        assertEquals(4.9, model.rating, 0.001)
        assertEquals(0.94, model.score!!, 0.001)
        assertEquals("Recomandat pentru preferințele tale de munte", model.reason)
        assertEquals(listOf("schi", "spa", "lux"), model.tags)
    }

    @Test
    fun bookingDtoMapsFieldsCorrectly() {
        val accDto = NetworkAccommodationDto(
            id = 10L,
            title = "Chalet Bella Vista",
            country = "Italia",
            region = "Dolomiți",
            city = "Cortina",
            type = "MOUNTAIN",
            pricePerNight = 180.0,
            imageUrl = "https://img.url/photo.jpg"
        )

        val bookingDto = NetworkBookingDto(
            id = 100L,
            accommodation = accDto,
            checkIn = "2026-11-01",
            checkOut = "2026-11-05",
            guests = 2,
            totalPrice = 720.0,
            status = "CONFIRMED",
            createdAt = "2026-10-26T12:00:00Z"
        )

        val model = bookingDto.toModel()

        assertEquals(100L, model.id)
        assertEquals(10L, model.accommodationId)
        assertEquals("Chalet Bella Vista", model.accommodationTitle)
        assertEquals("Cortina", model.accommodationCity)
        assertEquals("2026-11-01", model.checkIn)
        assertEquals("2026-11-05", model.checkOut)
        assertEquals(2, model.guests)
        assertEquals(720.0, model.totalPrice, 0.001)
        assertTrue(model.isConfirmed)
    }

    @Test
    fun userPreferencesDtoBidirectionalMapping() {
        val original = UserPreferences(
            preferredTypes = setOf(AccommodationType.SEA, AccommodationType.CITY),
            budgetMin = 75,
            budgetMax = 280,
            allInclusivePreference = AllInclusivePreference.NO,
            travelType = TravelType.FRIENDS
        )

        val dto = NetworkUserPreferenceDto.fromModel(original)
        val restored = dto.toModel()

        assertEquals(original.preferredTypes, restored.preferredTypes)
        assertEquals(original.budgetMin, restored.budgetMin)
        assertEquals(original.budgetMax, restored.budgetMax)
        assertEquals(original.allInclusivePreference, restored.allInclusivePreference)
        assertEquals(original.travelType, restored.travelType)
    }
}
