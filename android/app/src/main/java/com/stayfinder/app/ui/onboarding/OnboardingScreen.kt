package com.stayfinder.app.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stayfinder.app.model.AccommodationType
import com.stayfinder.app.model.AllInclusivePreference
import com.stayfinder.app.model.TravelType
import kotlin.math.roundToInt

/**
 * Ecranul de Onboarding in 4 pasi:
 *  1. Tip destinatie (Mare / Munte / Oras)
 *  2. Interval de buget cu RangeSlider (40..400 EUR/noapte)
 *  3. All-inclusive (Da / Nu / Indiferent)
 *  4. Tip de calatorie (Cuplu / Familie / Prieteni)
 *
 * La apasarea butonului "Finalizează", preferintele sunt salvate in Room (`user_preferences`)
 * iar flag-ul `onboarding_completed = true` este salvat in DataStore.
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Pasul ${state.currentStep + 1} din ${state.totalSteps}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (state.currentStep + 1).toFloat() / state.totalSteps.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.currentStep > 0) {
                        OutlinedButton(
                            onClick = viewModel::previousStep,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Înapoi")
                        }
                    }
                    val isLastStep = state.currentStep == state.totalSteps - 1
                    Button(
                        onClick = {
                            if (isLastStep) {
                                viewModel.completeOnboarding(onFinished)
                            } else {
                                viewModel.nextStep()
                            }
                        },
                        enabled = state.canAdvance && !state.isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isLastStep) "Descoperă cazările" else "Continuă")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (state.currentStep) {
                0 -> DestinationStep(
                    selected = state.preferredTypes,
                    onToggle = viewModel::toggleType
                )
                1 -> BudgetStep(
                    budgetMin = state.budgetMin,
                    budgetMax = state.budgetMax,
                    onRangeChanged = viewModel::updateBudgetRange
                )
                2 -> AllInclusiveStep(
                    selected = state.allInclusivePreference,
                    onSelect = viewModel::selectAllInclusive
                )
                else -> TravelTypeStep(
                    selected = state.travelType,
                    onSelect = viewModel::selectTravelType
                )
            }
        }
    }
}

@Composable
private fun DestinationStep(
    selected: Set<AccommodationType>,
    onToggle: (AccommodationType) -> Unit
) {
    StepHeader(
        title = "Ce tip de destinație preferi?",
        subtitle = "Poți alege una sau mai multe opțiuni pentru feed-ul tău personalizat."
    )
    val descriptions = mapOf(
        AccommodationType.MOUNTAIN to "Cabane alpine, pârtii de schi, drumeții și aer curat (Dolomiți, Alpi, Carpați)",
        AccommodationType.SEA to "Plaje însorite, insule, apusuri pe malul mării (Grecia, Croația, Spania)",
        AccommodationType.CITY to "City break cultural, gastronomie, muzee și arhitectură (Roma, Viena, Praga)"
    )
    AccommodationType.entries.forEach { type ->
        SelectableOptionCard(
            title = type.label,
            description = descriptions[type].orEmpty(),
            selected = type in selected,
            onClick = { onToggle(type) }
        )
    }
}

@Composable
private fun BudgetStep(
    budgetMin: Int,
    budgetMax: Int,
    onRangeChanged: (Int, Int) -> Unit
) {
    StepHeader(
        title = "Care este bugetul tău pe noapte?",
        subtitle = "Cazările din acest interval primesc scor maxim la recomandare."
    )
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "$budgetMin € – $budgetMax € / noapte",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            RangeSlider(
                value = budgetMin.toFloat()..budgetMax.toFloat(),
                onValueChange = { range ->
                    val min = (range.start / 5f).roundToInt() * 5
                    val max = (range.endInclusive / 5f).roundToInt() * 5
                    onRangeChanged(min, max.coerceAtLeast(min + 10))
                },
                valueRange = 40f..400f
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("40 € (buget)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("400 € (lux)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AllInclusiveStep(
    selected: AllInclusivePreference,
    onSelect: (AllInclusivePreference) -> Unit
) {
    StepHeader(
        title = "Preferi regim All-Inclusive?",
        subtitle = "Vom prioritiza cazările care se potrivesc stilului tău de vacanță."
    )
    val descriptions = mapOf(
        AllInclusivePreference.YES to "Vreau mese, băuturi și facilități incluse în pachet.",
        AllInclusivePreference.NO to "Prefer să explorez restaurantele și tavernele locale.",
        AllInclusivePreference.ANY to "Indiferent — arată-mi cele mai bune cazări din ambele categorii."
    )
    AllInclusivePreference.entries.forEach { pref ->
        SelectableOptionCard(
            title = pref.label,
            description = descriptions[pref].orEmpty(),
            selected = selected == pref,
            onClick = { onSelect(pref) }
        )
    }
}

@Composable
private fun TravelTypeStep(
    selected: TravelType,
    onSelect: (TravelType) -> Unit
) {
    StepHeader(
        title = "Cu cine călătorești de obicei?",
        subtitle = "Alegem cazări potrivite pentru atmosfera pe care o cauți."
    )
    val descriptions = mapOf(
        TravelType.COUPLE to "Escapade romantice, spa, liniște și priveliști panoramice.",
        TravelType.FAMILY to "Camere spațioase, piscine, locuri de joacă și acces facil.",
        TravelType.FRIENDS to "Aventură, pârtii, viață de noapte și apartamente de grup."
    )
    TravelType.entries.forEach { type ->
        SelectableOptionCard(
            title = type.label,
            description = descriptions[type].orEmpty(),
            selected = selected == type,
            onClick = { onSelect(type) }
        )
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SelectableOptionCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val border = if (selected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selectat",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}
