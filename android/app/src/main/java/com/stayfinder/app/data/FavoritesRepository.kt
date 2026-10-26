package com.stayfinder.app.data

import com.stayfinder.app.data.db.FavoriteDao
import com.stayfinder.app.data.db.FavoriteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Favoritele utilizatorului, ca flux observabil.
 *
 * De ce Flow/StateFlow: mai multe ecrane (feed, detaliu, favorite) afiseaza
 * starea "inimioarei". Toate observa ACELASI flux, deci cand o cazare e
 * salvata pe ecranul de detaliu, feed-ul se actualizeaza automat.
 */
interface FavoritesRepository {
    val favoriteIds: StateFlow<Set<Long>>
    suspend fun toggle(accommodationId: Long)
    suspend fun syncWithBackend()
}

/**
 * Implementare persistenta cu Room si sincronizare backend (Faza 5):
 * favoritele apar instantaneu din SQLite-ul local (offline-first),
 * iar apelurile catre /api/me/favorites sincronizeaza datele cu serverul.
 */
class RoomFavoritesRepository(
    private val favoriteDao: FavoriteDao,
    private val apiServiceProvider: (() -> com.stayfinder.app.data.network.StayFinderApiService)? = null
) : FavoritesRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val favoriteIds: StateFlow<Set<Long>> =
        favoriteDao.observeFavoriteIds()
            .map { it.toSet() }
            .stateIn(scope, SharingStarted.Eagerly, emptySet())

    override suspend fun syncWithBackend() {
        val apiService = apiServiceProvider?.invoke() ?: return
        try {
            val remoteFavorites = apiService.getFavorites()
            val remoteIds = remoteFavorites.map { it.accommodation.id }.toSet()
            // Sincronizăm în Room
            for (id in remoteIds) {
                if (!favoriteDao.isFavorite(id)) {
                    favoriteDao.insert(FavoriteEntity(accommodationId = id))
                }
            }
        } catch (_: Exception) {
            // Dacă eșuează rețeaua, Room rămâne sursa de adevăr locală
        }
    }

    override suspend fun toggle(accommodationId: Long) {
        val isCurrentlyFavorite = favoriteDao.isFavorite(accommodationId)
        if (isCurrentlyFavorite) {
            favoriteDao.deleteById(accommodationId)
            try {
                apiServiceProvider?.invoke()?.removeFavorite(accommodationId)
            } catch (_: Exception) {
                // Rămâne șters local
            }
        } else {
            favoriteDao.insert(FavoriteEntity(accommodationId = accommodationId))
            try {
                apiServiceProvider?.invoke()?.addFavorite(accommodationId)
            } catch (_: Exception) {
                // Rămâne salvat local
            }
        }
    }
}

/** Implementare in memorie pastrata pentru teste unitare rapide pe JVM. */
class InMemoryFavoritesRepository : FavoritesRepository {

    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
    override val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

    override suspend fun toggle(accommodationId: Long) {
        _favoriteIds.update { current ->
            if (accommodationId in current) current - accommodationId else current + accommodationId
        }
    }

    override suspend fun syncWithBackend() {
        // No-op în memorie
    }
}
