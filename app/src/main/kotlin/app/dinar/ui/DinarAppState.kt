package app.dinar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import app.dinar.feature.budgets.navigation.navigateToBudgets
import app.dinar.feature.home.navigation.navigateToHome
import app.dinar.feature.reports.navigation.navigateToReports
import app.dinar.feature.transactions.navigation.navigateToTransactions
import app.dinar.navigation.TopLevelDestination

@Composable
fun rememberDinarAppState(
    navController: NavHostController = rememberNavController(),
): DinarAppState = remember(navController) { DinarAppState(navController) }

/** UI-level state and navigation logic, following the Now in Android pattern. */
@Stable
class DinarAppState(val navController: NavHostController) {

    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() = currentDestination.let { destination ->
            TopLevelDestination.entries.firstOrNull { destination.isRouteInHierarchy(it) }
        }

    fun navigateToTopLevelDestination(destination: TopLevelDestination) {
        val options = navOptions {
            // Keep one copy of each tab on the back stack and remember its state.
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        when (destination) {
            TopLevelDestination.HOME -> navController.navigateToHome(options)
            TopLevelDestination.TRANSACTIONS -> navController.navigateToTransactions(options)
            TopLevelDestination.REPORTS -> navController.navigateToReports(options)
            TopLevelDestination.BUDGETS -> navController.navigateToBudgets(options)
        }
    }
}

fun NavDestination?.isRouteInHierarchy(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route) } ?: false
