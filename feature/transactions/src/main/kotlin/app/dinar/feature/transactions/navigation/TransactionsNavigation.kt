package app.dinar.feature.transactions.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import app.dinar.feature.transactions.TransactionsRoute as TransactionsScreenRoute
import kotlinx.serialization.Serializable

@Serializable
data object TransactionsRoute

fun NavController.navigateToTransactions(navOptions: NavOptions? = null) =
    navigate(TransactionsRoute, navOptions)

fun NavGraphBuilder.transactionsScreen(
    onTransactionClick: (Long) -> Unit,
    onAddTransaction: () -> Unit,
) {
    composable<TransactionsRoute> {
        TransactionsScreenRoute(onTransactionClick = onTransactionClick, onAddTransaction = onAddTransaction)
    }
}
