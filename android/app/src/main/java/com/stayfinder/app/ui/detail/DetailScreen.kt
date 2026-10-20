package com.stayfinder.app.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.stayfinder.app.model.Accommodation
import com.stayfinder.app.ui.components.ErrorView
import com.stayfinder.app.ui.components.FavoriteButton
import com.stayfinder.app.ui.components.LoadingView
import com.stayfinder.app.ui.components.LocationLabel
import com.stayfinder.app.ui.components.PriceLabel
import com.stayfinder.app.ui.components.RatingLabel
import com.stayfinder.app.ui.components.ReasonLabel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Ecranul de detaliu al unei cazari: imagine mare, descriere, tag-uri,
 * locatie (cu deschidere in aplicatia de harti) si butonul "Rezervă".
 */
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = viewModel(factory = DetailViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Bara de jos apare doar cand avem datele cazarii
            (state as? DetailUiState.Success)?.let { success ->
                BookingBar(
                    pricePerNight = success.accommodation.pricePerNight,
                    onBook = {
                        // Rezervarea reala (alegere date, persoane, confirmare) vine in faza 5
                        scope.launch { snackbarHostState.showSnackbar("Rezervarea va fi disponibilă în curând") }
                    }
                )
            }
        }
    ) { innerPadding ->
        when (val s = state) {
            DetailUiState.Loading -> LoadingView(Modifier.statusBarsPadding())
            is DetailUiState.Error -> Box(Modifier.statusBarsPadding()) {
                ErrorView(message = s.message, onRetry = viewModel::load)
                BackButton(onBack, Modifier.padding(8.dp))
            }
            is DetailUiState.Success -> DetailContent(
                accommodation = s.accommodation,
                isFavorite = s.isFavorite,
                onBack = onBack,
                onToggleFavorite = viewModel::onToggleFavorite,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    accommodation: Accommodation,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ---- Imaginea mare, desenata si sub bara de status (edge-to-edge) ----
        Box {
            AsyncImage(
                model = accommodation.imageUrl,
                contentDescription = accommodation.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BackButton(onBack)
                FavoriteButton(isFavorite = isFavorite, onClick = onToggleFavorite)
            }
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---- Titlu, locatie, rating, pret ----
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(accommodation.title, style = MaterialTheme.typography.headlineSmall)
                LocationLabel("${accommodation.city}, ${accommodation.region}, ${accommodation.country}")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingLabel(accommodation.rating)
                    Spacer(Modifier.width(16.dp))
                    PriceLabel(accommodation.pricePerNight)
                }
            }

            accommodation.reason?.let { ReasonLabel(it) }

            // ---- Caracteristici ----
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChip(accommodation.type.label)
                InfoChip("Potrivit pentru: ${accommodation.suitableFor.label}")
                if (accommodation.allInclusive) InfoChip("All inclusive")
            }

            HorizontalDivider()

            // ---- Descriere ----
            Section(title = "Despre cazare") {
                Text(
                    accommodation.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ---- Tag-uri ----
            Section(title = "Ce găsești aici") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    accommodation.tags.forEach { tag ->
                        SuggestionChip(onClick = {}, label = { Text("#$tag") })
                    }
                }
            }

            // ---- Locatie ----
            Section(title = "Locație") {
                LocationCard(accommodation)
            }
        }
    }
}

/**
 * Locatia: coordonatele + buton care deschide aplicatia de harti instalata.
 * Folosim un Intent "geo:" in loc de un SDK de harti: zero chei API, zero configurare.
 */
@Composable
private fun LocationCard(accommodation: Accommodation) {
    val context = LocalContext.current
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(accommodation.city, style = MaterialTheme.typography.titleSmall)
                Text(
                    String.format(Locale.US, "%.4f, %.4f", accommodation.latitude, accommodation.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = {
                val lat = accommodation.latitude
                val lng = accommodation.longitude
                val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(accommodation.title)})")
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (e: ActivityNotFoundException) {
                    // Pe emulatoarele fara Google Maps nu exista aplicatie de harti: deschidem in browser
                    val web = Uri.parse("https://www.openstreetmap.org/?mlat=$lat&mlon=$lng#map=13/$lat/$lng")
                    context.startActivity(Intent(Intent.ACTION_VIEW, web))
                }
            }) {
                Text("Hartă")
            }
        }
    }
}

/** Bara fixa de jos: pretul + butonul principal de actiune. */
@Composable
private fun BookingBar(pricePerNight: Double, onBook: () -> Unit) {
    Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PriceLabel(pricePerNight, Modifier.weight(1f))
            Button(onClick = onBook, contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)) {
                Text("Rezervă")
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        modifier = modifier
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Înapoi")
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun InfoChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
