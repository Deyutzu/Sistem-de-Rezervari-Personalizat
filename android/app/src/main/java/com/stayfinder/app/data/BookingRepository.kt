package com.stayfinder.app.data

import com.stayfinder.app.data.network.CreateBookingRequestDto
import com.stayfinder.app.data.network.StayFinderApiService
import com.stayfinder.app.model.Booking

/**
 * Repository pentru operațiunile de rezervare ale utilizatorului.
 */
interface BookingRepository {
    suspend fun getBookings(): Result<List<Booking>>
    suspend fun createBooking(accommodationId: Long, checkIn: String, checkOut: String, guests: Int): Result<Booking>
    suspend fun cancelBooking(bookingId: Long): Result<Booking>
}

class NetworkBookingRepository(
    private val apiServiceProvider: () -> StayFinderApiService
) : BookingRepository {

    override suspend fun getBookings(): Result<List<Booking>> = runCatching {
        apiServiceProvider().getBookings().map { it.toModel() }
    }

    override suspend fun createBooking(
        accommodationId: Long,
        checkIn: String,
        checkOut: String,
        guests: Int
    ): Result<Booking> = runCatching {
        val request = CreateBookingRequestDto(accommodationId, checkIn, checkOut, guests)
        apiServiceProvider().createBooking(request).toModel()
    }

    override suspend fun cancelBooking(bookingId: Long): Result<Booking> = runCatching {
        apiServiceProvider().cancelBooking(bookingId).toModel()
    }
}
