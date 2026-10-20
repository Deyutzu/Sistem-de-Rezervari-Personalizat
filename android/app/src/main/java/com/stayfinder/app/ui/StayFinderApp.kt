package com.stayfinder.app.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stayfinder.app.StayFinderApplication
import com.stayfinder.app.ui.components.LoadingView
import com.stayfinder.app.ui.navigation.Routes
import com.stayfinder.app.ui.navigation.StayFinderNavHost
import com.stayfinder.app.ui.navigation.TopLevelDestination

/**
 * Scheletul (shell-ul) aplicatiei:
 * - verifica din DataStore daca utilizatorul a completat deja onboarding-ul;
 * - daca NU -> deschide OnboardingScreen in 4 pasi; daca DA -> deschide direct Feed-ul;
 * - afiseaza bara de navigare de jos pe cele 4 tab-uri principale (Feed, Favorite, Rezervari, Profil)
 *   si o ascunde pe ecranele Onboarding si Detaliu.
 */
@Composable
fun StayFinderApp() {
    val app = LocalContext.current.applicationContext as StayFinderApplication
    val onboardingCompleted by app.container.preferencesRepository
        .onboardingCompleted
        .collectAsStateWithLifecycle()

    // Asteptam citirea initiala din DataStore (cateva milisecunde) ca sa nu facem "flash" intre ecrane
    val completed = onboardingCompleted ?: run {
        LoadingView()
        return
    }
    // Fixam destinatia de start o singura data la deschiderea aplicatiei
    val startDestination = remember { if (completed) Routes.FEED else Routes.ONBOARDING }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val topDestinations = TopLevelDestination.entries
    val showBottomBar = topDestinations.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topDestinations.forEach { dest ->
                        val selected = currentRoute == dest.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        StayFinderNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .consumeWindowInsets(innerPadding)
        )
    }
}
