package com.stayfinder.app.model

/**
 * Modelul unei cazari in aplicatia Android.
 * Campurile corespund cu AccommodationDto din backend, ca in faza 5 JSON-ul
 * primit prin Retrofit sa se mapeze direct pe aceasta clasa.
 *
 * "data class" = Kotlin genereaza automat equals/hashCode/toString/copy,
 * esential pentru Compose: UI-ul se redeseneaza doar cand datele chiar se schimba.
 */
data class Accommodation(
    val id: Long,
    val title: String,
    val description: String,
    val country: String,
    val region: String,
    val city: String,
    val type: AccommodationType,
    val pricePerNight: Double,
    val allInclusive: Boolean,
    val rating: Double,
    val imageUrl: String,
    val latitude: Double,
    val longitude: Double,
    val tags: List<String>,
    val suitableFor: TravelType,
    // Explicatia recomandarii (ex. "Similar cu Dolomitii din Italia").
    // In faza 5 vine din /api/feed; pana atunci e text demonstrativ sau null.
    val reason: String? = null
)

/** Tipurile de destinatie; label = textul afisat in UI. */
enum class AccommodationType(val label: String) {
    SEA("Mare"),
    MOUNTAIN("Munte"),
    CITY("Oraș")
}

enum class TravelType(val label: String) {
    COUPLE("Cuplu"),
    FAMILY("Familie"),
    FRIENDS("Prieteni")
}
