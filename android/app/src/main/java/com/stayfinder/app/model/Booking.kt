package com.stayfinder.app.model

/**
 * Modelul unei rezervări în aplicația Android.
 */
data class Booking(
    val id: Long,
    val accommodationId: Long,
    val accommodationTitle: String,
    val accommodationCity: String,
    val accommodationCountry: String,
    val accommodationImageUrl: String,
    val checkIn: String,
    val checkOut: String,
    val guests: Int,
    val totalPrice: Double,
    val status: String,
    val createdAt: String
) {
    val isConfirmed: Boolean get() = status.equals("CONFIRMED", ignoreCase = true)
    val isCancelled: Boolean get() = status.equals("CANCELLED", ignoreCase = true)
}
