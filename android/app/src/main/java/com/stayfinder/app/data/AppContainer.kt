package com.stayfinder.app.data

import android.content.Context
import com.stayfinder.app.data.db.StayFinderDatabase

/**
 * "Containerul" de dependente al aplicatiei (Dependency Injection manual).
 *
 * De ce: ViewModel-urile au nevoie de repository-uri, dar nu trebuie sa le
 * construiasca singure. Le cream O SINGURA DATA aici si le impartim.
 * In Faza 3 am conectat baza de date Room (`StayFinderDatabase`) si DataStore
 * (`LocalPreferencesRepository`), astfel incat favoritele, preferintele si
 * cache-ul de cazari persista pe disc si functioneaza offline.
 */
class AppContainer(context: Context) {
    private val database: StayFinderDatabase = StayFinderDatabase.getInstance(context)

    val preferencesRepository: PreferencesRepository =
        LocalPreferencesRepository(context.applicationContext, database.userPreferenceDao())

    val favoritesRepository: FavoritesRepository =
        RoomFavoritesRepository(database.favoriteDao())

    val accommodationRepository: AccommodationRepository =
        OfflineFirstAccommodationRepository(
            accommodationDao = database.accommodationDao(),
            preferencesRepository = preferencesRepository
        )
}
