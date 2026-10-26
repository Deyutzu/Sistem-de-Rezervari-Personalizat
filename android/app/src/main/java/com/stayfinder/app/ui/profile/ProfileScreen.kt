package com.stayfinder.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stayfinder.app.model.AccommodationType
import com.stayfinder.app.model.AllInclusivePreference
import com.stayfinder.app.model.TravelType
import com.stayfinder.app.ui.onboarding.OnboardingViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Ecranul "Profil & Preferințe": permite vizualizarea si modificarea rapida
 * a preferintelor salvate in Room sau reluarea ghidului de onboarding in 4 pasi.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onRerunOnboarding: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as com.stayfinder.app.StayFinderApplication
    val currentUser by app.container.authRepository.currentUser.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profil & Preferințe") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card utilizator conectat
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = currentUser?.displayName ?: "Utilizator StayFinder",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = currentUser?.email ?: "Conectat prin token JWT",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                app.container.authRepository.logout()
                                onLogout()
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text("Deconectare")
                    }
                }
            }

            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Preferințele tale de călătorie", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Feed-ul „Pentru tine” este ordonat automat în funcție de aceste opțiuni, salvate local în Room.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    // 1. Tipuri destinatie
                    Text("Tipuri de destinație", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccommodationType.entries.forEach { type ->
                            FilterChip(
                                selected = type in state.preferredTypes,
                                onClick = { viewModel.toggleType(type) },
                                label = { Text(type.label) }
                            )
                        }
                    }

                    // 2. Buget
                    Text(
                        "Buget: ${state.budgetMin} € – ${state.budgetMax} € / noapte",
                        style = MaterialTheme.typography.labelLarge
                    )
                    RangeSlider(
                        value = state.budgetMin.toFloat()..state.budgetMax.toFloat(),
                        onValueChange = { range ->
                            val min = (range.start / 5f).roundToInt() * 5
                            val max = (range.endInclusive / 5f).roundToInt() * 5
                            viewModel.updateBudgetRange(min, max.coerceAtLeast(min + 10))
                        },
                        valueRange = 40f..400f
                    )

                    // 3. All-inclusive
                    Text("Regim All-Inclusive", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AllInclusivePreference.entries.forEach { pref ->
                            FilterChip(
                                selected = state.allInclusivePreference == pref,
                                onClick = { viewModel.selectAllInclusive(pref) },
                                label = { Text(pref.label) }
                            )
                        }
                    }

                    // 4. Tip calatorie
                    Text("Tip de călătorie", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TravelType.entries.forEach { type ->
                            FilterChip(
                                selected = state.travelType == type,
                                onClick = { viewModel.selectTravelType(type) },
                                label = { Text(type.label) }
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onRerunOnboarding,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ghid în 4 pași")
                        }
                        Button(
                            onClick = {
                                viewModel.completeOnboarding {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Preferințele au fost salvate!")
                                    }
                                }
                            },
                            enabled = state.canAdvance && !state.isSaving,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Salvează")
                        }
                    }
                }
            }
        }
    }
}
