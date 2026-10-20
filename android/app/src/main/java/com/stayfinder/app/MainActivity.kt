package com.stayfinder.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stayfinder.app.ui.StayFinderApp
import com.stayfinder.app.ui.theme.StayFinderTheme

/**
 * Singura Activity din aplicatie. Doar porneste UI-ul Compose;
 * toata logica de ecrane si navigare e in StayFinderApp.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Continutul se deseneaza si sub bara de status/navigare (aspect modern);
        // Scaffold-urile adauga automat spatierea necesara (insets).
        enableEdgeToEdge()
        setContent {
            StayFinderTheme {
                StayFinderApp()
            }
        }
    }
}
