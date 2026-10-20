package com.stayfinder.app.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stayfinder.app.ui.components.AccommodationCard
import com.stayfinder.app.ui.components.EmptyView
import com.stayfinder.app.ui.components.ErrorView
import com.stayfinder.app.ui.components.LoadingView

/** Ecranul "Favorite": refoloseste acelasi AccommodationCard ca feed-ul. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onAccommodationClick: (Long) -> Unit,
    viewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Favorite") }) }) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (val s = state) {
            FavoritesUiState.Loading -> LoadingView(modifier)
            is FavoritesUiState.Error -> ErrorView(s.message, onRetry = viewModel::load, modifier = modifier)
            is FavoritesUiState.Success ->
                if (s.items.isEmpty()) {
                    EmptyView(
                        icon = Icons.Default.FavoriteBorder,
                        title = "Nicio cazare salvată",
                        message = "Atinge inimioara de pe o cazare ca să o găsești rapid aici.",
                        modifier = modifier
                    )
                } else {
                    LazyColumn(
                        modifier = modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(s.items, key = { it.id }) { accommodation ->
                            AccommodationCard(
                                accommodation = accommodation,
                                isFavorite = true,
                                onClick = { onAccommodationClick(accommodation.id) },
                                onToggleFavorite = { viewModel.onToggleFavorite(accommodation.id) }
                            )
                        }
                    }
                }
        }
    }
}
