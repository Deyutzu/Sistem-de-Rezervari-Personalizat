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
import androidx.compose.runtime.setValue
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.stayfinder.app.model.Accommodation
import com.stayfinder.app.model.NearbyPlace
import com.stayfinder.app.model.WeatherForecast
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

    var showBookingDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var isBookingInProgress by remember { androidx.compose.runtime.mutableStateOf(false) }
    var bookingErrorMessage by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Bara de jos apare doar cand avem datele cazarii
            (state as? DetailUiState.Success)?.let { success ->
                BookingBar(
                    pricePerNight = success.accommodation.pricePerNight,
                    ronRate = success.ronRate,
                    onBook = {
                        bookingErrorMessage = null
                        showBookingDialog = true
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
            is DetailUiState.Success -> {
                DetailContent(
                    accommodation = s.accommodation,
                    isFavorite = s.isFavorite,
                    weather = s.weather,
                    ronRate = s.ronRate,
                    nearbyPlaces = s.nearbyPlaces,
                    onBack = onBack,
                    onToggleFavorite = viewModel::onToggleFavorite,
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )

                if (showBookingDialog) {
                    BookingDialog(
                        accommodation = s.accommodation,
                        isBooking = isBookingInProgress,
                        errorMessage = bookingErrorMessage,
                        onDismiss = {
                            if (!isBookingInProgress) showBookingDialog = false
                        },
                        onConfirm = { checkIn, checkOut, guests ->
                            isBookingInProgress = true
                            bookingErrorMessage = null
                            scope.launch {
                                val result = viewModel.bookStay(checkIn, checkOut, guests)
                                isBookingInProgress = false
                                result.onSuccess {
                                    showBookingDialog = false
                                    snackbarHostState.showSnackbar("Rezervare confirmată cu succes!")
                                }.onFailure { error ->
                                    bookingErrorMessage = error.message ?: "Eroare la crearea rezervării. Verifică datele."
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    accommodation: Accommodation,
    isFavorite: Boolean,
    weather: WeatherForecast?,
    ronRate: Double?,
    nearbyPlaces: List<NearbyPlace>,
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
                    Column {
                        PriceLabel(accommodation.pricePerNight)
                        if (ronRate != null) {
                            val priceRon = accommodation.pricePerNight * ronRate
                            Text(
                                text = String.format(Locale.US, "~ %.0f RON / noapte", priceRon),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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

            // ---- Vremea în următoarele zile (Open-Meteo) ----
            if (weather != null && weather.days.isNotEmpty()) {
                HorizontalDivider()
                Section(title = "Vremea în următoarele zile") {
                    WeatherSection(weather)
                }
            }

            // ---- Obiective turistice aproape (OpenTripMap) ----
            if (nearbyPlaces.isNotEmpty()) {
                HorizontalDivider()
                Section(title = "Aproape de cazare (rază 5 km)") {
                    NearbyPlacesSection(nearbyPlaces)
                }
            }
        }
    }
}

/** Rând orizontal de carduri meteo cu iconiță, min/max și timestamp de cache. */
@Composable
private fun WeatherSection(weather: WeatherForecast) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val timeFormatted = remember(weather.updatedAt) {
            val sdf = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.format(java.util.Date(weather.updatedAt))
        }
        Text(
            text = "Actualizat la $timeFormatted • ${weather.note}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(weather.days) { day ->
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .width(88.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = day.date.takeLast(5).replace("-", "."),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val iconChar = when (day.icon) {
                            "sun" -> "☀️"
                            "clouds" -> "⛅"
                            "rain" -> "🌧️"
                            "snow" -> "❄️"
                            "thunder" -> "⛈️"
                            else -> "🌤️"
                        }
                        Text(iconChar, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = day.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                        Text(
                            text = "${day.minTemp.toInt()}° / ${day.maxTemp.toInt()}°",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        if (day.precipitation > 0) {
                            Text(
                                text = "💧 ${day.precipitation}mm",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Lista de obiective turistice în apropiere. */
@Composable
private fun NearbyPlacesSection(places: List<NearbyPlace>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        places.forEach { place ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(place.name, style = MaterialTheme.typography.titleSmall)
                        Text(place.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    val distStr = if (place.distanceMeters >= 1000) {
                        String.format(Locale.US, "%.1f km", place.distanceMeters / 1000.0)
                    } else {
                        "${place.distanceMeters.toInt()} m"
                    }
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = distStr,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
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

/** Bara fixa de jos: pretul (EUR + aproximare RON) + butonul principal de actiune. */
@Composable
private fun BookingBar(pricePerNight: Double, ronRate: Double?, onBook: () -> Unit) {
    Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                PriceLabel(pricePerNight)
                if (ronRate != null) {
                    val priceRon = pricePerNight * ronRate
                    Text(
                        text = String.format(Locale.US, "~ %.0f RON / noapte", priceRon),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
