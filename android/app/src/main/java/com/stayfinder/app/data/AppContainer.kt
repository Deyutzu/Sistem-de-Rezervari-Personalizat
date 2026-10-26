package com.stayfinder.app.data

import android.content.Context
import com.stayfinder.app.data.db.StayFinderDatabase
import com.stayfinder.app.data.network.NetworkClient
import com.stayfinder.app.data.network.StayFinderApiService

/**
 * "Containerul" de dependențe al aplicației (Dependency Injection manual).
 *
 * În Faza 5 integrăm:
 *  - Retrofit + Moshi (`StayFinderApiService`) pentru comunicare REST cu backend-ul Spring Boot;
 *  - `AuthInterceptor` care atașează automat JWT-ul și gestionează expirarea (401);
 *  - `AuthRepository` pentru stocarea token-ului în DataStore și login/register;
 *  - `BookingRepository` pentru rezervări simulate;
 *  - Room (`StayFinderDatabase`) pentru persistență și funcționare offline-first.
 */
class AppContainer(context: Context) {
    private val database: StayFinderDatabase = StayFinderDatabase.getInstance(context)

    // ApiService-ul este creat leneș cu referință la tokenProvider din authRepository
    val apiService: StayFinderApiService by lazy {
        NetworkClient.createApiService(
            tokenProvider = { authRepository.currentToken },
            onUnauthorized = { authRepository.clearTokenSync() }
        )
    }

    val authRepository: AuthRepository =
        DefaultAuthRepository(
            context = context.applicationContext,
            apiServiceProvider = { apiService }
        )

    val bookingRepository: BookingRepository =
        NetworkBookingRepository(
            apiServiceProvider = { apiService }
        )

    val preferencesRepository: PreferencesRepository =
        LocalPreferencesRepository(
            context = context.applicationContext,
            userPreferenceDao = database.userPreferenceDao(),
            apiServiceProvider = { apiService }
        )

    val favoritesRepository: FavoritesRepository =
        RoomFavoritesRepository(
            favoriteDao = database.favoriteDao(),
            apiServiceProvider = { apiService }
        )

    val accommodationRepository: AccommodationRepository =
        OfflineFirstAccommodationRepository(
            accommodationDao = database.accommodationDao(),
            preferencesRepository = preferencesRepository,
            apiServiceProvider = { apiService },
            authRepository = authRepository
        )
}
