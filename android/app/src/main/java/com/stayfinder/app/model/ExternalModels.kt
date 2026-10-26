package com.stayfinder.app.model

/**
 * Modele de domeniu pentru integrările externe (vreme, obiective turistice).
 */

data class DailyWeather(
    val date: String,
    val minTemp: Double,
    val maxTemp: Double,
    val precipitation: Double,
    val description: String,
    val icon: String
)

data class WeatherForecast(
    val accommodationId: Long,
    val locationName: String,
    val note: String,
    val days: List<DailyWeather>,
    val updatedAt: Long
)

data class NearbyPlace(
    val name: String,
    val category: String,
    val distanceMeters: Double
)
