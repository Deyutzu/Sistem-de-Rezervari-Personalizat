package com.stayfinder.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
 * Faza 2: favoritele sunt tinute doar in memorie (se pierd la inchiderea aplicatiei).
 * Faza 3: le inlocuim cu o implementare Room, care le pastreaza si offline.
 */
class InMemoryFavoritesRepository : FavoritesRepository {

    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
    override val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

    override suspend fun toggle(accommodationId: Long) {
        // update{} modifica atomic valoarea (sigur si daca e apelat din mai multe corutine)
        _favoriteIds.update { current ->
            if (accommodationId in current) current - accommodationId else current + accommodationId
        }
    }
}
