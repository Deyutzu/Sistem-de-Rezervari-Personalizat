package com.stayfinder.app

import com.stayfinder.app.data.AccommodationRepository
import com.stayfinder.app.data.FakeAccommodationRepository
import com.stayfinder.app.data.InMemoryFavoritesRepository
import com.stayfinder.app.data.OfflineFirstAccommodationRepository
import com.stayfinder.app.data.PreferencesRepository
import com.stayfinder.app.data.db.AccommodationDao
import com.stayfinder.app.data.db.AccommodationEntity
import com.stayfinder.app.data.db.UserPreferenceEntity
import com.stayfinder.app.model.Accommodation
import com.stayfinder.app.model.AccommodationType
import com.stayfinder.app.model.AllInclusivePreference
import com.stayfinder.app.model.TravelType
import com.stayfinder.app.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste unitare rapide pe JVM pentru repository-urile din Fazele 2 si 3.
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

    @Test
    fun entityMappingPreservesTagsAndPreferences() {
        val prefs = UserPreferences(
            preferredTypes = setOf(AccommodationType.SEA, AccommodationType.CITY),
            budgetMin = 80,
            budgetMax = 220,
            allInclusivePreference = AllInclusivePreference.YES,
            travelType = TravelType.FAMILY
        )
        val restored = UserPreferenceEntity.fromModel(prefs).toModel()
        assertEquals(prefs, restored)
    }

    @Test
    fun offlineFirstRepositoryFallsBackToRoomCacheAndPersonalizesOrder() = runBlocking {
        val fakeDao = object : AccommodationDao {
            private val store = mutableMapOf<Long, AccommodationEntity>()
            override suspend fun getAll(): List<AccommodationEntity> = store.values.toList()
            override suspend fun getById(id: Long): AccommodationEntity? = store[id]
            override suspend fun upsertAll(items: List<AccommodationEntity>) {
                items.forEach { store[it.id] = it }
            }
            override suspend fun count(): Int = store.size
        }

        val fakePrefs = object : PreferencesRepository {
            override val onboardingCompleted: StateFlow<Boolean?> = MutableStateFlow(true)
            override val userPreferences: StateFlow<UserPreferences> = MutableStateFlow(
                UserPreferences(
                    preferredTypes = setOf(AccommodationType.SEA),
                    budgetMin = 100,
                    budgetMax = 260,
                    allInclusivePreference = AllInclusivePreference.YES,
                    travelType = TravelType.FAMILY
                )
            )
            override suspend fun savePreferences(preferences: UserPreferences) {}
        }

        var shouldFailNetwork = false
        val controllableRemote = object : AccommodationRepository {
            private val delegate = FakeAccommodationRepository()
            override suspend fun getAccommodations(): List<Accommodation> {
                if (shouldFailNetwork) throw RuntimeException("No internet")
                return delegate.getAccommodations()
            }
            override suspend fun getAccommodation(id: Long): Accommodation? = delegate.getAccommodation(id)
        }

        val repo = OfflineFirstAccommodationRepository(fakeDao, fakePrefs, controllableRemote)

        // 1. Prima incarcare populeaza cache-ul Room si ordoneaza dupa preferintele SEA + FAMILY + AllInclusive
        val onlineList = repo.getAccommodations()
        assertEquals(12, fakeDao.count())
        // Belek Grand Resort (SEA, 230 EUR, allInclusive=true, FAMILY) trebuie sa fie pe primul loc
        assertEquals("Belek Grand Resort", onlineList.first().title)

        // 2. Oprim reteaua: repository-ul returneaza cazarile din cache-ul Room fara eroare
        shouldFailNetwork = true
        val offlineList = repo.getAccommodations()
        assertEquals(12, offlineList.size)
        assertEquals("Belek Grand Resort", offlineList.first().title)
    }
}
