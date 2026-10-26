package com.stayfinder.app.ui.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.stayfinder.app.model.Booking
import com.stayfinder.app.ui.components.EmptyView
import com.stayfinder.app.ui.components.ErrorView
import com.stayfinder.app.ui.components.LoadingView
import com.stayfinder.app.ui.components.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    onAccommodationClick: (Long) -> Unit,
    viewModel: BookingsViewModel = viewModel(factory = BookingsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rezervările mele") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> LoadingView()

                state.errorMessage != null && state.bookings.isEmpty() ->
                    ErrorView(message = state.errorMessage!!, onRetry = viewModel::load)

                else -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (state.bookings.isEmpty()) {
                        EmptyView(
                            icon = Icons.Default.DateRange,
                            title = "Nicio rezervare încă",
                            message = "Alege o cazare din feed și rezervă sejurul tău de vis!"
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.bookings, key = { it.id }) { booking ->
                                BookingCard(
                                    booking = booking,
                                    isCancelling = state.cancellingId == booking.id,
                                    onClick = { onAccommodationClick(booking.accommodationId) },
                                    onCancel = { viewModel.cancelBooking(booking.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    isCancelling: Boolean,
    onClick: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = booking.accommodationImageUrl,
                    contentDescription = booking.accommodationTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = booking.accommodationTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        BookingStatusChip(status = booking.status)
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${booking.accommodationCity}, ${booking.accommodationCountry}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${booking.checkIn}  →  ${booking.checkOut}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${booking.guests} oaspeți  •  Total: ${formatPrice(booking.totalPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (booking.isConfirmed) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        enabled = !isCancelling,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (isCancelling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Anulează rezervarea")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingStatusChip(status: String) {
    val isConfirmed = status.equals("CONFIRMED", ignoreCase = true)
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isConfirmed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
        contentColor = if (isConfirmed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    ) {
        Text(
            text = if (isConfirmed) "CONFIRMATĂ" else "ANULATĂ",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
