package com.stayfinder.app.data.network

import retrofit2.Response
import retrofit2.http.*

/**
 * Interfața Retrofit cu toate endpoint-urile REST ale backend-ului Spring Boot.
 */
interface StayFinderApiService {

    // --- Autentificare ---
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    // --- Cazări & Feed Recomandări ---
    @GET("api/feed")
    suspend fun getFeed(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponseDto<NetworkAccommodationDto>

    @GET("api/accommodations")
    suspend fun getAccommodations(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponseDto<NetworkAccommodationDto>

    @GET("api/accommodations/{id}")
    suspend fun getAccommodation(@Path("id") id: Long): NetworkAccommodationDto

    // --- Preferințe utilizator curent ---
    @GET("api/me/preferences")
    suspend fun getPreferences(): NetworkUserPreferenceDto

    @PUT("api/me/preferences")
    suspend fun updatePreferences(@Body preferences: NetworkUserPreferenceDto): NetworkUserPreferenceDto

    // --- Favorite utilizator curent ---
    @GET("api/me/favorites")
    suspend fun getFavorites(): List<NetworkFavoriteDto>

    @POST("api/me/favorites/{id}")
    suspend fun addFavorite(@Path("id") accommodationId: Long): NetworkFavoriteDto

    @DELETE("api/me/favorites/{id}")
    suspend fun removeFavorite(@Path("id") accommodationId: Long): Response<Unit>

    // --- Rezervări utilizator curent ---
    @GET("api/me/bookings")
    suspend fun getBookings(): List<NetworkBookingDto>

    @POST("api/me/bookings")
    suspend fun createBooking(@Body request: CreateBookingRequestDto): NetworkBookingDto

    @PUT("api/me/bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: Long): NetworkBookingDto

    // --- Faza 5C: Integrări externe expuse prin backend ---
    @GET("api/accommodations/{id}/weather")
    suspend fun getWeather(@Path("id") accommodationId: Long): NetworkWeatherForecastDto

    @GET("api/exchange-rates")
    suspend fun getExchangeRates(@Query("base") base: String = "EUR"): NetworkExchangeRatesDto

    @GET("api/accommodations/{id}/nearby")
    suspend fun getNearbyPlaces(@Path("id") accommodationId: Long): List<NetworkNearbyPlaceDto>
}
