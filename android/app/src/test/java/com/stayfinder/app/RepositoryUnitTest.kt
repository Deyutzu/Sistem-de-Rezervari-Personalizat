package com.stayfinder.app

import com.stayfinder.app.data.FakeAccommodationRepository
import com.stayfinder.app.data.InMemoryFavoritesRepository
import com.stayfinder.app.model.AccommodationType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste unitare rapide pe JVM pentru repository-urile din Faza 2.
 */
class RepositoryUnitTest {

    @Test
    fun fakeRepositoryReturnsSeedItemsAndDetail() = runBlocking {
        val repo = FakeAccommodationRepository()
        val list = repo.getAccommodations()
        assertEquals(12, list.size)
        assertTrue(list.any { it.type == AccommodationType.MOUNTAIN })
        assertTrue(list.any { it.type == AccommodationType.SEA })
        assertTrue(list.any { it.type == AccommodationType.CITY })

        val detail = repo.getAccommodation(7L)
        assertNotNull(detail)
        assertEquals("Dolomitenhof Lienz", detail?.title)
    }

    @Test
    fun favoritesRepositoryTogglesIdempotently() = runBlocking {
        val favs = InMemoryFavoritesRepository()
        assertFalse(1L in favs.favoriteIds.value)

        favs.toggle(1L)
        assertTrue(1L in favs.favoriteIds.value)

        favs.toggle(1L)
        assertFalse(1L in favs.favoriteIds.value)
    }
}
