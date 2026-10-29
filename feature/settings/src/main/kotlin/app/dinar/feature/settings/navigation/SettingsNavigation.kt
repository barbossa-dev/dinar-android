package app.dinar.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.dinar.feature.settings.SettingsRoute as SettingsScreenRoute
import kotlinx.serialization.Serializable

@Serializable
data object SettingsRoute

fun NavController.navigateToSettings() = navigate(SettingsRoute) { launchSingleTop = true }

fun NavGraphBuilder.settingsScreen(onBack: () -> Unit) {
    composable<SettingsRoute> { SettingsScreenRoute(onBack = onBack) }
}
