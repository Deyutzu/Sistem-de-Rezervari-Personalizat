package com.stayfinder.app.data.network

import com.stayfinder.app.model.*

/**
 * DTO-uri pentru cereri și răspunsuri HTTP (Retrofit + Moshi).
 * Reflectă exact structurile JSON expuse de backend-ul Spring Boot.
 */

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val displayName: String
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class UserDto(
    val id: Long,
    val email: String,
    val displayName: String,
    val role: String
) {
    fun toModel(): User = User(id = id, email = email, displayName = displayName, role = role)
}

data class AuthResponseDto(
    val token: String,
    val user: UserDto
)

data class NetworkAccommodationDto(
    val id: Long,
    val title: String,
    val description: String? = null,
    val country: String,
    val region: String,
    val city: String,
    val type: String,
    val pricePerNight: Double,
    val allInclusive: Boolean = false,
    val rating: Double = 0.0,
    val imageUrl: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val tags: List<String> = emptyList(),
    val suitableFor: String? = null,
    val score: Double? = null,
    val reason: String? = null
) {
    fun toModel(): Accommodation = Accommodation(
        id = id,
        title = title,
        description = description.orEmpty(),
        country = country,
        region = region,
        city = city,
        type = runCatching { AccommodationType.valueOf(type) }.getOrDefault(AccommodationType.MOUNTAIN),
        pricePerNight = pricePerNight,
        allInclusive = allInclusive,
        rating = rating,
        imageUrl = imageUrl.orEmpty(),
        latitude = latitude,
        longitude = longitude,
        tags = tags,
        suitableFor = runCatching { TravelType.valueOf(suitableFor ?: "COUPLE") }.getOrDefault(TravelType.COUPLE),
        score = score,
        reason = reason
    )
}

data class PageResponseDto<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)

data class NetworkUserPreferenceDto(
    val preferredTypes: Set<String>,
    val budgetMin: Double? = null,
    val budgetMax: Double? = null,
    val allInclusivePreference: String? = null,
    val travelType: String? = null
) {
    fun toModel(): UserPreferences = UserPreferences(
        preferredTypes = preferredTypes.mapNotNull {
            runCatching { AccommodationType.valueOf(it) }.getOrNull()
        }.toSet().ifEmpty { setOf(AccommodationType.MOUNTAIN) },
        budgetMin = budgetMin?.toInt() ?: 60,
        budgetMax = budgetMax?.toInt() ?: 250,
        allInclusivePreference = runCatching {
            AllInclusivePreference.valueOf(allInclusivePreference ?: "ANY")
        }.getOrDefault(AllInclusivePreference.ANY),
        travelType = runCatching {
            TravelType.valueOf(travelType ?: "COUPLE")
        }.getOrDefault(TravelType.COUPLE)
    )

    companion object {
        fun fromModel(model: UserPreferences): NetworkUserPreferenceDto = NetworkUserPreferenceDto(
            preferredTypes = model.preferredTypes.map { it.name }.toSet(),
            budgetMin = model.budgetMin.toDouble(),
            budgetMax = model.budgetMax.toDouble(),
            allInclusivePreference = model.allInclusivePreference.name,
            travelType = model.travelType.name
        )
    }
}

data class NetworkFavoriteDto(
    val id: Long,
    val accommodation: NetworkAccommodationDto,
    val createdAt: String
)

data class CreateBookingRequestDto(
    val accommodationId: Long,
    val checkIn: String,
    val checkOut: String,
    val guests: Int
)

data class NetworkBookingDto(
    val id: Long,
    val accommodation: NetworkAccommodationDto,
    val checkIn: String,
    val checkOut: String,
    val guests: Int,
    val totalPrice: Double,
    val status: String,
    val createdAt: String
) {
    fun toModel(): Booking = Booking(
        id = id,
        accommodationId = accommodation.id,
        accommodationTitle = accommodation.title,
        accommodationCity = accommodation.city,
        accommodationCountry = accommodation.country,
        accommodationImageUrl = accommodation.imageUrl.orEmpty(),
        checkIn = checkIn,
        checkOut = checkOut,
        guests = guests,
        totalPrice = totalPrice,
        status = status,
        createdAt = createdAt
    )
}
