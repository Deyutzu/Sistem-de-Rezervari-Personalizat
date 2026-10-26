package com.stayfinder.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stayfinder.app.StayFinderApplication
import com.stayfinder.app.data.AccommodationRepository
import com.stayfinder.app.data.ExternalDataRepository
import com.stayfinder.app.data.FavoritesRepository
import com.stayfinder.app.model.Accommodation
import com.stayfinder.app.model.NearbyPlace
import com.stayfinder.app.model.WeatherForecast
import com.stayfinder.app.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Starea ecranului de detaliu. sealed = setul de stari posibile e inchis si cunoscut. */
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Error(val message: String) : DetailUiState
    data class Success(
        val accommodation: Accommodation,
        val isFavorite: Boolean,
        val weather: WeatherForecast? = null,
        val ronRate: Double? = null,
        val rateUpdatedAt: Long? = null,
        val nearbyPlaces: List<NearbyPlace> = emptyList()
    ) : DetailUiState
}

/**
 * ViewModel-ul ecranului de detaliu.
 *
 * Gestionează încărcarea datelor cazării, sincronizarea stării de favorite,
 * precum și afișarea prognozei meteo (Open-Meteo), conversiei în RON (Frankfurter)
 * și a obiectivelor din jur (OpenTripMap) salvate în cache local Room.
 */
class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val accommodationRepository: AccommodationRepository,
    private val favoritesRepository: FavoritesRepository,
    private val bookingRepository: com.stayfinder.app.data.BookingRepository,
    private val externalDataRepository: ExternalDataRepository
) : ViewModel() {

    private val accommodationId: Long = checkNotNull(savedStateHandle[Routes.ARG_ID])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        load()

        // Sincronizare favorite
        viewModelScope.launch {
            favoritesRepository.favoriteIds.collect { ids ->
                _uiState.update { state ->
                    if (state is DetailUiState.Success) state.copy(isFavorite = accommodationId in ids) else state
                }
            }
        }

        // Observare vreme din Room cache (cu re-emitere automată la refresh)
        viewModelScope.launch {
            externalDataRepository.observeWeather(accommodationId).collect { forecast ->
                _uiState.update { state ->
                    if (state is DetailUiState.Success) state.copy(weather = forecast) else state
                }
            }
        }

        // Observare curs valutar EUR -> RON din Room cache
        viewModelScope.launch {
            externalDataRepository.observeExchangeRate("EUR").collect { rateEntity ->
                _uiState.update { state ->
                    if (state is DetailUiState.Success) {
                        state.copy(
                            ronRate = rateEntity?.ronRate,
                            rateUpdatedAt = rateEntity?.updatedAt
                        )
                    } else state
                }
            }
        }
    }

    fun load() {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                val accommodation = accommodationRepository.getAccommodation(accommodationId)
                if (accommodation == null) {
                    DetailUiState.Error("Cazarea nu mai este disponibilă.")
                } else {
                    val isFavorite = accommodationId in favoritesRepository.favoriteIds.value
                    DetailUiState.Success(accommodation, isFavorite)
                }
            } catch (e: Exception) {
                DetailUiState.Error("Nu am putut încărca detaliile. Încearcă din nou.")
            }

            // Declanșăm actualizările externe în fundal dacă cazarea a fost încărcată
            if (_uiState.value is DetailUiState.Success) {
                launch { externalDataRepository.refreshWeather(accommodationId) }
                launch { externalDataRepository.refreshExchangeRate("EUR") }
                launch {
                    val places = externalDataRepository.getNearbyPlaces(accommodationId)
                    _uiState.update { state ->
                        if (state is DetailUiState.Success) state.copy(nearbyPlaces = places) else state
                    }
                }
            }
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(accommodationId) }
    }

    suspend fun bookStay(checkIn: String, checkOut: String, guests: Int): Result<com.stayfinder.app.model.Booking> {
        return bookingRepository.createBooking(accommodationId, checkIn, checkOut, guests)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as StayFinderApplication
                DetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    accommodationRepository = app.container.accommodationRepository,
                    favoritesRepository = app.container.favoritesRepository,
                    bookingRepository = app.container.bookingRepository,
                    externalDataRepository = app.container.externalDataRepository
                )
            }
        }
    }
}
