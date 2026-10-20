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
}

/**
 * Implementare persistenta cu Room (Faza 3): favoritele raman salvate in SQLite
 * si dupa inchiderea aplicatiei / repornirea telefonului.
 */
class RoomFavoritesRepository(
    private val favoriteDao: FavoriteDao
) : FavoritesRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val favoriteIds: StateFlow<Set<Long>> =
        favoriteDao.observeFavoriteIds()
            .map { it.toSet() }
            .stateIn(scope, SharingStarted.Eagerly, emptySet())

    override suspend fun toggle(accommodationId: Long) {
        if (favoriteDao.isFavorite(accommodationId)) {
            favoriteDao.deleteById(accommodationId)
        } else {
            favoriteDao.insert(FavoriteEntity(accommodationId = accommodationId))
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
}
