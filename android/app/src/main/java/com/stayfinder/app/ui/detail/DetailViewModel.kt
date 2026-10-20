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
import com.stayfinder.app.data.FavoritesRepository
import com.stayfinder.app.model.Accommodation
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
    data class Success(val accommodation: Accommodation, val isFavorite: Boolean) : DetailUiState
}

/**
 * ViewModel-ul ecranului de detaliu.
 *
 * Id-ul cazarii vine din ruta de navigare ("accommodation/{id}") prin SavedStateHandle,
 * care pastreaza argumentele si dupa ce sistemul omoara procesul in fundal.
 */
class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val accommodationRepository: AccommodationRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val accommodationId: Long = checkNotNull(savedStateHandle[Routes.ARG_ID])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        load()
        // Tinem inimioara sincronizata cu favoritele (pot fi schimbate si din alt ecran)
        viewModelScope.launch {
            favoritesRepository.favoriteIds.collect { ids ->
                _uiState.update { state ->
                    if (state is DetailUiState.Success) state.copy(isFavorite = accommodationId in ids) else state
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
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch { favoritesRepository.toggle(accommodationId) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as StayFinderApplication
                DetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    accommodationRepository = app.container.accommodationRepository,
                    favoritesRepository = app.container.favoritesRepository
                )
            }
        }
    }
}
