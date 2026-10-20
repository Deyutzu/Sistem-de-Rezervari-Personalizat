package com.stayfinder.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.stayfinder.app.ui.components.EmptyView
import com.stayfinder.app.ui.detail.DetailScreen
import com.stayfinder.app.ui.favorites.FavoritesScreen
import com.stayfinder.app.ui.feed.FeedScreen
import com.stayfinder.app.ui.onboarding.OnboardingScreen
import com.stayfinder.app.ui.profile.ProfileScreen

/**
 * Harta de navigare: ce ecran corespunde fiecarei rute.
 * NavHost afiseaza ecranul rutei curente si tine "back stack"-ul
 * (butonul Back revine automat la ecranul anterior).
 */
@Composable
fun StayFinderNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.FEED) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FEED) {
            FeedScreen(onAccommodationClick = { id -> navController.navigate(Routes.detail(id)) })
        }

        composable(Routes.FAVORITES) {
            FavoritesScreen(onAccommodationClick = { id -> navController.navigate(Routes.detail(id)) })
        }

        composable(Routes.BOOKINGS) {
            ComingSoonScreen(
                title = "Rezervări",
                message = "Aici vei vedea rezervările tale."
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onRerunOnboarding = { navController.navigate(Routes.ONBOARDING) }
            )
        }

        // Ruta cu argument: id-ul e declarat Long, iar Navigation il pune
        // automat in SavedStateHandle-ul DetailViewModel-ului.
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.LongType })
        ) {
            DetailScreen(onBack = { navController.popBackStack() })
        }
    }
}

/** Ecran temporar pentru tab-urile implementate in fazele urmatoare. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComingSoonScreen(title: String, message: String) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { padding ->
        EmptyView(
            icon = if (title == "Profil") Icons.Filled.Person else Icons.Filled.DateRange,
            title = "În curând",
            message = message,
            modifier = Modifier.padding(padding)
        )
    }
}
