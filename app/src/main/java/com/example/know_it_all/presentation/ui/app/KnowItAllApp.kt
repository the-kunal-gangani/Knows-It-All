package com.example.know_it_all.presentation.ui.app

import androidx.compose.runtime.Composable
import com.example.know_it_all.presentation.ui.navigation.KnowItAllNavigation
import com.example.know_it_all.ui.theme.KnowItAllTheme

/**
 * Root composable — wraps the theme and navigation.
 * Called from MainActivity.setContent if you prefer a dedicated
 * App composable over calling KnowItAllNavigation directly.
 *
 * Fix: removed the `application` parameter that was causing
 * "No parameter with name 'application' found" — KnowItAllApplication
 * is accessed via LocalContext inside KnowItAllNavigation, not passed down.
 */
@Composable
fun KnowItAllApp() {
    KnowItAllTheme {
        KnowItAllNavigation()
    }
}