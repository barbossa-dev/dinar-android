package app.dinar.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import app.dinar.core.model.TransactionType
import app.dinar.feature.home.HomeRoute as HomeScreenRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

fun NavController.navigateToHome(navOptions: NavOptions? = null) = navigate(HomeRoute, navOptions)

fun NavGraphBuilder.homeScreen(
    onAddTransaction: (TransactionType) -> Unit,
    onTransactionClick: (Long) -> Unit,
    onSeeAllTransactions: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBudgets: () -> Unit,
) {
    composable<HomeRoute> {
        HomeScreenRoute(
            onAddTransaction = onAddTransaction,
            onTransactionClick = onTransactionClick,
            onSeeAllTransactions = onSeeAllTransactions,
            onOpenSettings = onOpenSettings,
            onOpenBudgets = onOpenBudgets,
        )
    }
}
