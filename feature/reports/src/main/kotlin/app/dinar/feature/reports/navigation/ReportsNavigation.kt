package app.dinar.feature.reports.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import app.dinar.feature.reports.ReportsRoute as ReportsScreenRoute
import kotlinx.serialization.Serializable

@Serializable
data object ReportsRoute

fun NavController.navigateToReports(navOptions: NavOptions? = null) = navigate(ReportsRoute, navOptions)

fun NavGraphBuilder.reportsScreen() {
    composable<ReportsRoute> { ReportsScreenRoute() }
}
