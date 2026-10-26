package com.stayfinder.app.ui.feed

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stayfinder.app.model.AccommodationType
import com.stayfinder.app.ui.components.AccommodationCard
import com.stayfinder.app.ui.components.EmptyView
import com.stayfinder.app.ui.components.ErrorView
import com.stayfinder.app.ui.components.LoadingView

/**
 * Ecranul "Pentru tine": lista de cazari sub forma de carduri.
 *
 * Primeste doar un callback de navigare (onAccommodationClick), nu NavController-ul:
 * ecranul nu stie CUM se navigheaza, doar anunta ca s-a dat click.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onAccommodationClick: (Long) -> Unit,
    viewModel: FeedViewModel = viewModel(factory = FeedViewModel.Factory)
) {
    // collectAsStateWithLifecycle: colecteaza fluxul doar cat timp ecranul e vizibil (economie de baterie)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Bara de sus mare care se strange la scroll
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Pentru tine")
                        Text(
                            "Cazări alese după gusturile tale",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            TypeFilterRow(selected = state.selectedType, onSelected = viewModel::onTypeSelected)

            if (state.isOffline) {
                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Mod offline: date servite din cache-ul local Room",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // "when" alege ce desenam in functie de stare: loading / eroare / gol / lista
            when {
                state.isLoading -> LoadingView()

                state.errorMessage != null && state.allItems.isEmpty() ->
                    ErrorView(message = state.errorMessage!!, onRetry = viewModel::load)

                else -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (state.visibleItems.isEmpty()) {
                        EmptyView(
                            icon = Icons.Default.Search,
                            title = "Nicio cazare găsită",
                            message = "Încearcă un alt tip de destinație."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // key = id: Compose recunoaste fiecare card la reordonare/filtrare
                            // si nu le redeseneaza inutil
                            items(state.visibleItems, key = { it.id }) { accommodation ->
                                AccommodationCard(
                                    accommodation = accommodation,
                                    isFavorite = accommodation.id in state.favoriteIds,
                                    onClick = { onAccommodationClick(accommodation.id) },
                                    onToggleFavorite = { viewModel.onToggleFavorite(accommodation.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Rand orizontal de filtre: Toate / Mare / Munte / Oras. */
@Composable
private fun TypeFilterRow(
    selected: AccommodationType?,
    onSelected: (AccommodationType?) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelected(null) },
            label = { Text("Toate") }
        )
        AccommodationType.entries.forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelected(type) },
                label = { Text(type.label) }
            )
        }
    }
}
