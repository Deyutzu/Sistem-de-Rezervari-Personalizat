package com.stayfinder.app.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stayfinder.app.model.Accommodation
import com.stayfinder.app.ui.components.formatPrice
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun BookingDialog(
    accommodation: Accommodation,
    onDismiss: () -> Unit,
    onConfirm: (checkIn: String, checkOut: String, guests: Int) -> Unit,
    isBooking: Boolean = false,
    errorMessage: String? = null
) {
    // Valori implicite plauzibile (peste 10 zile, sejur de 4 nopți)
    var checkInDate by remember { mutableStateOf(LocalDate.now().plusDays(10).toString()) }
    var checkOutDate by remember { mutableStateOf(LocalDate.now().plusDays(14).toString()) }
    var guests by remember { mutableIntStateOf(2) }

    val nights = remember(checkInDate, checkOutDate) {
        runCatching {
            val inDate = LocalDate.parse(checkInDate)
            val outDate = LocalDate.parse(checkOutDate)
            ChronoUnit.DAYS.between(inDate, outDate).coerceAtLeast(1)
        }.getOrDefault(4L)
    }

    val totalPrice = nights * accommodation.pricePerNight

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Rezervare ${accommodation.title}", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Plata este SIMULATĂ. Rezervarea ta va fi confirmată instantaneu pe server.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = checkInDate,
                    onValueChange = { checkInDate = it },
                    label = { Text("Check-in (AAAA-LL-ZZ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = checkOutDate,
                    onValueChange = { checkOutDate = it },
                    label = { Text("Check-out (AAAA-LL-ZZ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Număr oaspeți:")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(
                            onClick = { if (guests > 1) guests-- },
                            enabled = guests > 1
                        ) {
                            Text("-")
                        }
                        Text(
                            text = "$guests",
                            modifier = Modifier.padding(horizontal = 12.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        FilledTonalIconButton(
                            onClick = { if (guests < 6) guests++ },
                            enabled = guests < 6
                        ) {
                            Text("+")
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Preț total ($nights nopți):", fontWeight = FontWeight.Bold)
                    Text(
                        formatPrice(totalPrice),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(checkInDate, checkOutDate, guests) },
                enabled = !isBooking
            ) {
                if (isBooking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Confirmă Rezervarea")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBooking) {
                Text("Renunță")
            }
        }
    )
}
