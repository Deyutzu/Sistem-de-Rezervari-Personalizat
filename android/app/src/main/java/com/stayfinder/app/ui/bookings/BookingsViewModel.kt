package com.stayfinder.app.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stayfinder.app.StayFinderApplication
import com.stayfinder.app.data.BookingRepository
import com.stayfinder.app.model.Booking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookingsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val bookings: List<Booking> = emptyList(),
    val errorMessage: String? = null,
    val cancellingId: Long? = null
)

class BookingsViewModel(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingsUiState())
    val uiState: StateFlow<BookingsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        fetch()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        fetch()
    }

    private fun fetch() {
        viewModelScope.launch {
            val result = bookingRepository.getBookings()
            result.onSuccess { list ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        bookings = list,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = error.message ?: "Nu am putut încărca rezervările. Verifică dacă ești autentificat."
                    )
                }
            }
        }
    }

    fun cancelBooking(id: Long) {
        _uiState.update { it.copy(cancellingId = id) }
        viewModelScope.launch {
            val result = bookingRepository.cancelBooking(id)
            result.onSuccess { updatedBooking ->
                _uiState.update { state ->
                    state.copy(
                        cancellingId = null,
                        bookings = state.bookings.map {
                            if (it.id == updatedBooking.id) updatedBooking else it
                        }
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(cancellingId = null) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as StayFinderApplication
                BookingsViewModel(app.container.bookingRepository)
            }
        }
    }
}
