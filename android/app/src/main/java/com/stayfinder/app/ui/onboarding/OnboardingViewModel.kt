package com.stayfinder.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stayfinder.app.StayFinderApplication
import com.stayfinder.app.data.PreferencesRepository
import com.stayfinder.app.model.AccommodationType
import com.stayfinder.app.model.AllInclusivePreference
import com.stayfinder.app.model.TravelType
import com.stayfinder.app.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Starea pentru fluxul de onboarding (si pentru editarea preferintelor din Profil).
 * Cei 4 pasi ceruti in specificatie:
 *  - Pasul 0: Tip destinatie (Mare / Munte / Oras - selectie multipla)
 *  - Pasul 1: Interval de buget / noapte (RangeSlider 40..400 EUR)
 *  - Pasul 2: All-inclusive (Da / Nu / Indiferent)
 *  - Pasul 3: Tip de calatorie (Cuplu / Familie / Prieteni)
 */
data class OnboardingUiState(
    val currentStep: Int = 0,
    val totalSteps: Int = 4,
    val preferredTypes: Set<AccommodationType> = setOf(AccommodationType.MOUNTAIN, AccommodationType.SEA),
    val budgetMin: Int = 60,
    val budgetMax: Int = 250,
    val allInclusivePreference: AllInclusivePreference = AllInclusivePreference.ANY,
    val travelType: TravelType = TravelType.COUPLE,
    val isSaving: Boolean = false
) {
    val canAdvance: Boolean
        get() = preferredTypes.isNotEmpty() && budgetMin <= budgetMax
}

class OnboardingViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        // Pre-populam starea cu preferintele salvate anterior (util si la re-editare din Profil)
        viewModelScope.launch {
            preferencesRepository.userPreferences.collect { prefs ->
                _uiState.update {
                    it.copy(
                        preferredTypes = prefs.preferredTypes,
                        budgetMin = prefs.budgetMin,
                        budgetMax = prefs.budgetMax,
                        allInclusivePreference = prefs.allInclusivePreference,
                        travelType = prefs.travelType
                    )
                }
            }
        }
    }

    fun toggleType(type: AccommodationType) {
        _uiState.update { state ->
            val current = state.preferredTypes
            // Nu lasam setul complet gol: pastram cel putin un tip selectat
            val next = if (type in current && current.size > 1) current - type else current + type
            state.copy(preferredTypes = next)
        }
    }

    fun updateBudgetRange(min: Int, max: Int) {
        _uiState.update {
            it.copy(
                budgetMin = min.coerceIn(40, 400),
                budgetMax = max.coerceIn(min.coerceAtLeast(40), 400)
            )
        }
    }

    fun selectAllInclusive(pref: AllInclusivePreference) {
        _uiState.update { it.copy(allInclusivePreference = pref) }
    }

    fun selectTravelType(type: TravelType) {
        _uiState.update { it.copy(travelType = type) }
    }

    fun nextStep() {
        _uiState.update { state ->
            if (state.currentStep < state.totalSteps - 1) {
                state.copy(currentStep = state.currentStep + 1)
            } else state
        }
    }

    fun previousStep() {
        _uiState.update { state ->
            if (state.currentStep > 0) {
                state.copy(currentStep = state.currentStep - 1)
            } else state
        }
    }

    fun completeOnboarding(onCompleted: () -> Unit) {
        val s = _uiState.value
        if (!s.canAdvance || s.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            preferencesRepository.savePreferences(
                UserPreferences(
                    preferredTypes = s.preferredTypes,
                    budgetMin = s.budgetMin,
                    budgetMax = s.budgetMax,
                    allInclusivePreference = s.allInclusivePreference,
                    travelType = s.travelType
                )
            )
            _uiState.update { it.copy(isSaving = false) }
            onCompleted()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as StayFinderApplication
                OnboardingViewModel(app.container.preferencesRepository)
            }
        }
    }
}
