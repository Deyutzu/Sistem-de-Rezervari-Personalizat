package com.stayfinder.app.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stayfinder.app.StayFinderApplication
import com.stayfinder.app.data.AccommodationRepository
import com.stayfinder.app.data.FavoritesRepository
import com.stayfinder.app.model.Accommodation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data class Error(val message: String) : FavoritesUiState
    data class Success(val items: List<Accommodation>) : FavoritesUiState
}

/**
 * Lista de favorite = toate cazarile, filtrate dupa id-urile salvate.
 *
 * combine() uneste doua fluxuri: de fiecare data cand se schimba lista de cazari
 * SAU setul de favorite, starea se recalculeaza automat.
 */
class FavoritesViewModel(
    private val accommodationRepository: AccommodationRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    // null = inca se incarca; Result tine fie lista, fie eroarea
    private val allItems = MutableStateFlow<Result<List<Accommodation>>?>(null)

    val uiState: StateFlow<FavoritesUiState> =
        combine(allItems, favoritesRepository.favoriteIds) { result, ids ->
            when {
                result == null -> FavoritesUiState.Loading
                result.isFailure -> FavoritesUiState.Error("Nu am putut încărca favoritele.")
                else -> FavoritesUiState.Success(result.getOrThrow().filter { it.id in ids })
            }
        }.stateIn(
            scope = viewModelScope,
            // fluxul ramane activ inca 5s dupa ce UI-ul dispare (ex. la rotire), apoi se opreste
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState.Loading
        )

    init {
        load()
    }

    fun load() {
        allItems.value = null
        viewModelScope.launch {
            allItems.value = runCatching { accommodationRepository.getAccommodations() }
        }
    }

    /** Scoaterea de la favorite: cardul dispare singur, pentru ca fluxul favoriteIds se schimba. */
    fun onToggleFavorite(id: Long) {
        viewModelScope.launch { favoritesRepository.toggle(id) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as StayFinderApplication
                FavoritesViewModel(app.container.accommodationRepository, app.container.favoritesRepository)
            }
        }
    }
}
