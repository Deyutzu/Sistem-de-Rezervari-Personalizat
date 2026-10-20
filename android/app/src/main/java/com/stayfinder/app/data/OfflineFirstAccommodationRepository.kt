package com.stayfinder.app.data

import com.stayfinder.app.data.db.AccommodationDao
import com.stayfinder.app.data.db.AccommodationEntity
import com.stayfinder.app.model.Accommodation
import com.stayfinder.app.model.AllInclusivePreference
import com.stayfinder.app.model.UserPreferences

/**
 * Repository care combina sursa "remote" (in Faza 3 FakeAccommodationRepository,
 * iar in Faza 5 API-ul Retrofit) cu baza de date locala Room (`AccommodationDao`).
 *
 * Strategia offline-first:
 *  1. Incearca sa obtina datele din sursa remote si le salveaza in Room (`upsertAll`).
 *  2. Daca sursa remote esueaza (ex. telefonul e offline) sau la pornire, citeste din
 *     cache-ul Room (`accommodationDao.getAll()`).
 *  3. Ordinea si campul `reason` sunt adaptate dinamic dupa preferintele utilizatorului
 *     salvate la onboarding.
 */
class OfflineFirstAccommodationRepository(
    private val accommodationDao: AccommodationDao,
    private val preferencesRepository: PreferencesRepository,
    private val remoteSource: AccommodationRepository = FakeAccommodationRepository()
) : AccommodationRepository {

    override suspend fun getAccommodations(): List<Accommodation> {
        val list = try {
            val remote = remoteSource.getAccommodations()
            accommodationDao.upsertAll(remote.map(AccommodationEntity::fromModel))
            remote
        } catch (e: Exception) {
            val cached = accommodationDao.getAll().map(AccommodationEntity::toModel)
            if (cached.isNotEmpty()) cached else throw e
        }
        val prefs = preferencesRepository.userPreferences.value
        return rankByPreferences(list, prefs)
    }

    override suspend fun getAccommodation(id: Long): Accommodation? {
        val item = accommodationDao.getById(id)?.toModel()
            ?: runCatching { remoteSource.getAccommodation(id) }.getOrNull()?.also {
                accommodationDao.upsertAll(listOf(AccommodationEntity.fromModel(it)))
            }
        return item?.let { personalizeReason(it, preferencesRepository.userPreferences.value) }
    }

    /**
     * Ordoneaza cazarile local in functie de preferintele alese la onboarding
     * (pana cand in Faza 5 preluam scorul direct din `/api/feed` de pe backend).
     */
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
