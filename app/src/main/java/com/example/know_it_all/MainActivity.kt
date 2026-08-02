package com.example.know_it_all

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavController
import com.example.know_it_all.presentation.ui.navigation.KnowItAllNavigation
import com.example.know_it_all.ui.theme.KnowItAllTheme
import com.example.know_it_all.util.DeepLinkHandler

/**
 * Location: MainActivity.kt (root package)
 *
 * Handles both cold-start deep links (via onCreate intent)
 * and warm-start deep links (via onNewIntent).
 */
class MainActivity : ComponentActivity() {

    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KnowItAllTheme {
                KnowItAllNavigation(
                    onNavControllerReady = { navController = it },
                    pendingIntent = intent
                )
            }
        }
    }

    // Called when app is already running and a deep link is tapped
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navController?.let { nav ->
            DeepLinkHandler.handle(intent, nav)
        }
    }
}