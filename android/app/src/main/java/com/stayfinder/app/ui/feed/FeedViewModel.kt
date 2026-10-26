package com.stayfinder.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stayfinder.app.StayFinderApplication
import com.stayfinder.app.data.AccommodationRepository
import com.stayfinder.app.data.FavoritesRepository
import com.stayfinder.app.data.PreferencesRepository
import com.stayfinder.app.model.Accommodation
import com.stayfinder.app.model.AccommodationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Tot ce are nevoie ecranul Feed ca sa se deseneze, intr-un singur obiect imutabil.
 * UI-ul doar "citeste" aceasta stare; nu o modifica direct.
 */
data class FeedUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val allItems: List<Accommodation> = emptyList(),
    val selectedType: AccommodationType? = null, // null = "Toate"
    val favoriteIds: Set<Long> = emptySet()
) {
    /** Lista afisata, dupa aplicarea filtrului pe tip. Calculata, nu stocata. */
    val visibleItems: List<Accommodation>
        get() = if (selectedType == null) allItems else allItems.filter { it.type == selectedType }
}

/**
 * ViewModel-ul ecranului "Pentru tine".
 *
 * De ce ViewModel: supravietuieste rotirii ecranului (Activity e recreata, ViewModel nu),
 * deci nu reincarcam datele la fiecare rotire. viewModelScope anuleaza automat
 * corutinele cand ecranul e inchis definitiv (fara memory leak-uri).
 *
 * Flux unidirectional (UDF): UI -> eveniment (onTypeSelected) -> ViewModel
 * actualizeaza starea -> UI se redeseneaza din noua stare.
 */
class FeedViewModel(
    private val accommodationRepository: AccommodationRepository,
    private val favoritesRepository: FavoritesRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    // MutableStateFlow e privat: doar ViewModel-ul poate modifica starea.
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        load()
        // Observăm modul offline
        viewModelScope.launch {
            accommodationRepository.isOfflineMode.collect { offline ->
                _uiState.update { it.copy(isOffline = offline) }
            }
        }
        // Observam favoritele: orice schimbare (de pe orice ecran) ajunge in starea feed-ului.
        viewModelScope.launch {
            favoritesRepository.favoriteIds.collect { ids ->
                _uiState.update { it.copy(favoriteIds = ids) }
            }
        }
        // Sincronizăm favoritele cu backend-ul la pornire
        viewModelScope.launch {
            favoritesRepository.syncWithBackend()
        }
        // Daca utilizatorul isi modifica preferintele (la onboarding sau din Profil),
        // reincarcam si reordonam feed-ul.
        viewModelScope.launch {
            preferencesRepository.userPreferences.drop(1).collect {
                refresh()
            }
        }
    }

    /** Incarcare initiala sau "Reincearca" dupa eroare. */
    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        fetch()
    }

    /** Pull-to-refresh: pastram lista vizibila cat timp se reincarca. */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        fetch()
    }

    private fun fetch() {
        viewModelScope.launch {
            try {
                val items = accommodationRepository.getAccommodations()
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, allItems = items, errorMessage = null)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "Nu am putut încărca cazările. Verifică conexiunea și încearcă din nou."
                    )
                }
            }
        }
    }

    fun onTypeSelected(type: AccommodationType?) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun onToggleFavorite(id: Long) {
        viewModelScope.launch { favoritesRepository.toggle(id) }
    }

    companion object {
        /**
         * Factory = "reteta" prin care sistemul creeaza ViewModel-ul cu dependentele lui.
         * Le luam din AppContainer, prin clasa Application.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as StayFinderApplication
                FeedViewModel(
                    app.container.accommodationRepository,
                    app.container.favoritesRepository,
                    app.container.preferencesRepository
                )
            }
        }
    }
}
