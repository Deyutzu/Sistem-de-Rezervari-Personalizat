package com.stayfinder.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stayfinder.app.model.Accommodation
import com.stayfinder.app.model.AccommodationType
import com.stayfinder.app.model.AllInclusivePreference
import com.stayfinder.app.model.TravelType
import com.stayfinder.app.model.UserPreferences

/**
 * Tabelul local "accommodations": pastreaza cache-ul cazarilor in SQLite,
 * astfel incat aplicatia sa afiseze datele si la repornire sau offline.
 *
 * De ce entitati Room separate de modelele din pachetul `model`:
 * SQLite stocheaza tipuri primitive (String, Int, Double), asa ca listele (tags)
 * si enum-urile sunt serializate simplu in coloane text, fara a polua modelul UI.
 */
@Entity(tableName = "accommodations")
data class AccommodationEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val description: String,
    val country: String,
    val region: String,
    val city: String,
    val type: String,
    val pricePerNight: Double,
    val allInclusive: Boolean,
    val rating: Double,
    val imageUrl: String,
    val latitude: Double,
    val longitude: Double,
    val tagsCsv: String,
    val suitableFor: String,
    val reason: String?
) {
    fun toModel(): Accommodation = Accommodation(
        id = id,
        title = title,
        description = description,
        country = country,
        region = region,
        city = city,
        type = runCatching { AccommodationType.valueOf(type) }.getOrDefault(AccommodationType.MOUNTAIN),
        pricePerNight = pricePerNight,
        allInclusive = allInclusive,
        rating = rating,
        imageUrl = imageUrl,
        latitude = latitude,
        longitude = longitude,
        tags = if (tagsCsv.isBlank()) emptyList() else tagsCsv.split("|"),
        suitableFor = runCatching { TravelType.valueOf(suitableFor) }.getOrDefault(TravelType.COUPLE),
        reason = reason
    )

    companion object {
        fun fromModel(a: Accommodation): AccommodationEntity = AccommodationEntity(
            id = a.id,
            title = a.title,
            description = a.description,
            country = a.country,
            region = a.region,
            city = a.city,
            type = a.type.name,
            pricePerNight = a.pricePerNight,
            allInclusive = a.allInclusive,
            rating = a.rating,
            imageUrl = a.imageUrl,
            latitude = a.latitude,
            longitude = a.longitude,
            tagsCsv = a.tags.joinToString("|"),
            suitableFor = a.suitableFor.name,
            reason = a.reason
        )
    }
}

/**
 * Tabelul "favorites": contine id-urile cazarilor salvate la favorite.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val accommodationId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Tabelul "user_preferences": un singur rand (id = 1) cu preferintele utilizatorului.
 */
@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey val id: Int = 1,
    val preferredTypesCsv: String,
    val budgetMin: Int,
    val budgetMax: Int,
    val allInclusivePreference: String,
    val travelType: String
) {
    fun toModel(): UserPreferences {
        val types = preferredTypesCsv
            .split(",")
            .mapNotNull { name -> runCatching { AccommodationType.valueOf(name.trim()) }.getOrNull() }
            .toSet()
        return UserPreferences(
            preferredTypes = types.ifEmpty { setOf(AccommodationType.MOUNTAIN) },
            budgetMin = budgetMin,
            budgetMax = budgetMax,
            allInclusivePreference = runCatching { AllInclusivePreference.valueOf(allInclusivePreference) }
                .getOrDefault(AllInclusivePreference.ANY),
            travelType = runCatching { TravelType.valueOf(travelType) }
                .getOrDefault(TravelType.COUPLE)
        )
    }

    companion object {
        fun fromModel(prefs: UserPreferences): UserPreferenceEntity = UserPreferenceEntity(
            id = 1,
            preferredTypesCsv = prefs.preferredTypes.joinToString(",") { it.name },
            budgetMin = prefs.budgetMin,
            budgetMax = prefs.budgetMax,
            allInclusivePreference = prefs.allInclusivePreference.name,
            travelType = prefs.travelType.name
        )
    }
}
