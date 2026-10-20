package com.stayfinder.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Toate rutele (adresele ecranelor) intr-un singur loc, ca sa nu scriem
 * string-uri "magice" imprastiate prin cod.
 * O ruta seamana cu un URL: "accommodation/{id}" are un argument, id.
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val FEED = "feed"
    const val FAVORITES = "favorites"
    const val BOOKINGS = "bookings"
    const val PROFILE = "profile"

    const val ARG_ID = "id"
    const val DETAIL = "accommodation/{$ARG_ID}"

    /** Construieste ruta concreta, ex. detail(7) = "accommodation/7". */
    fun detail(id: Long) = "accommodation/$id"
}

/** Tab-urile din bara de navigare de jos. */
enum class TopLevelDestination(val route: String, val label: String, val icon: ImageVector) {
    FEED(Routes.FEED, "Pentru tine", Icons.Filled.Home),
    FAVORITES(Routes.FAVORITES, "Favorite", Icons.Filled.Favorite),
    BOOKINGS(Routes.BOOKINGS, "Rezervări", Icons.Filled.DateRange),
    PROFILE(Routes.PROFILE, "Profil", Icons.Filled.Person)
}
