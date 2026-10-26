package com.stayfinder.app.data

import com.stayfinder.app.data.db.AccommodationDao
import com.stayfinder.app.data.db.AccommodationEntity
import com.stayfinder.app.data.network.StayFinderApiService
import com.stayfinder.app.model.Accommodation
import com.stayfinder.app.model.AllInclusivePreference
import com.stayfinder.app.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository care combina sursa remote (API-ul Retrofit catre backend Spring Boot)
 * cu baza de date locala Room (`AccommodationDao`).
 *
 * Strategia offline-first:
 *  1. Daca avem retea, citim din `/api/feed` (personalizat cu scor si motiv)
 *     sau `/api/accommodations` si salvam automat rezultatul in Room (`upsertAll`).
 *  2. Daca reteaua este indisponibila, citim instantaneu din cache-ul Room
 *     si activam indicatorul discret `isOfflineMode = true`.
 */
class OfflineFirstAccommodationRepository(
    private val accommodationDao: AccommodationDao,
    private val preferencesRepository: PreferencesRepository,
    private val fallbackSource: AccommodationRepository = FakeAccommodationRepository(),
    private val apiServiceProvider: (() -> StayFinderApiService)? = null,
    private val authRepository: AuthRepository? = null
) : AccommodationRepository {

    private val _isOfflineMode = MutableStateFlow(false)
    override val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    override suspend fun getAccommodations(): List<Accommodation> {
        val apiService = apiServiceProvider?.invoke()

        if (apiService != null) {
            try {
                val isLoggedIn = authRepository?.isLoggedIn?.value ?: false
                val remoteDtos = if (isLoggedIn) {
                    apiService.getFeed(page = 0, size = 50).content
                } else {
                    apiService.getAccommodations(page = 0, size = 50).content
                }
                val remoteAccommodations = remoteDtos.map { it.toModel() }

                if (remoteAccommodations.isNotEmpty()) {
                    accommodationDao.upsertAll(remoteAccommodations.map(AccommodationEntity::fromModel))
                    _isOfflineMode.value = false
                    return remoteAccommodations
                }
            } catch (_: Exception) {
                // Eroare de rețea -> activăm modul offline și citim din Room
                val cached = accommodationDao.getAll().map(AccommodationEntity::toModel)
                if (cached.isNotEmpty()) {
                    _isOfflineMode.value = true
                    val prefs = preferencesRepository.userPreferences.value
                    return rankByPreferences(cached, prefs)
                }
            }
        }

        // Dacă nu avem API sau Room e gol la prima instalare offline, apelăm fallbackSource
        return try {
            val list = fallbackSource.getAccommodations()
            accommodationDao.upsertAll(list.map(AccommodationEntity::fromModel))
            _isOfflineMode.value = false
            val prefs = preferencesRepository.userPreferences.value
            rankByPreferences(list, prefs)
        } catch (e: Exception) {
            val cached = accommodationDao.getAll().map(AccommodationEntity::toModel)
            if (cached.isNotEmpty()) {
                _isOfflineMode.value = true
                val prefs = preferencesRepository.userPreferences.value
                rankByPreferences(cached, prefs)
            } else {
                throw e
            }
        }
    }

    override suspend fun getAccommodation(id: Long): Accommodation? {
        val apiService = apiServiceProvider?.invoke()
        if (apiService != null) {
            try {
                val dto = apiService.getAccommodation(id)
                val model = dto.toModel()
                accommodationDao.upsertAll(listOf(AccommodationEntity.fromModel(model)))
                _isOfflineMode.value = false
                return model
            } catch (_: Exception) {
                // Eroare de rețea: căutăm în Room
            }
        }

        val fromRoom = accommodationDao.getById(id)?.toModel()
        if (fromRoom != null) {
            _isOfflineMode.value = true
            return personalizeReason(fromRoom, preferencesRepository.userPreferences.value)
        }

        return fallbackSource.getAccommodation(id)
    }

    private fun rankByPreferences(
        items: List<Accommodation>,
        prefs: UserPreferences
    ): List<Accommodation> {
        return items
            .map { acc -> acc to scoreFor(acc, prefs) }
            .sortedByDescending { (_, score) -> score }
            .map { (acc, _) -> personalizeReason(acc, prefs) }
    }

    private fun scoreFor(a: Accommodation, p: UserPreferences): Double {
        val typeMatch = if (a.type in p.preferredTypes) 1.0 else 0.0
        val budgetMatch = when {
            a.pricePerNight in p.budgetMin.toDouble()..p.budgetMax.toDouble() -> 1.0
            a.pricePerNight < p.budgetMin -> (1.0 - (p.budgetMin - a.pricePerNight) / p.budgetMin.coerceAtLeast(1)).coerceAtLeast(0.0)
            else -> (1.0 - (a.pricePerNight - p.budgetMax) / p.budgetMax.coerceAtLeast(1)).coerceAtLeast(0.0)
        }
        val allIncMatch = when (p.allInclusivePreference) {
            AllInclusivePreference.ANY -> 1.0
            AllInclusivePreference.YES -> if (a.allInclusive) 1.0 else 0.0
            AllInclusivePreference.NO -> if (!a.allInclusive) 1.0 else 0.0
        }
        val travelMatch = if (a.suitableFor == p.travelType) 1.0 else 0.0
        return 0.35 * typeMatch + 0.30 * budgetMatch + 0.15 * allIncMatch + 0.15 * travelMatch + 0.05 * (a.rating / 5.0)
    }

    private fun personalizeReason(a: Accommodation, p: UserPreferences): Accommodation {
        if (!a.reason.isNullOrBlank()) return a
        val reasons = mutableListOf<String>()
        if (a.type in p.preferredTypes) reasons.add(a.type.label.lowercase())
        if (a.pricePerNight in p.budgetMin.toDouble()..p.budgetMax.toDouble()) {
            reasons.add("în bugetul tău (${p.budgetMin}–${p.budgetMax} €)")
        }
        if (a.suitableFor == p.travelType) {
            reasons.add("ideal pentru ${p.travelType.label.lowercase()}")
        }
        val text = if (reasons.isNotEmpty()) {
            "Recomandat: " + reasons.joinToString(", ")
        } else null
        return a.copy(reason = text)
    }
}
