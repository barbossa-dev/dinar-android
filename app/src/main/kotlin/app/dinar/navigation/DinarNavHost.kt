package app.dinar.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import app.dinar.feature.budgets.navigation.budgetsScreen
import app.dinar.feature.editor.navigation.editorScreen
import app.dinar.feature.editor.navigation.navigateToEditTransaction
import app.dinar.feature.editor.navigation.navigateToNewTransaction
import app.dinar.feature.home.navigation.HomeRoute
import app.dinar.feature.home.navigation.homeScreen
import app.dinar.feature.reports.navigation.reportsScreen
import app.dinar.feature.settings.navigation.navigateToSettings
import app.dinar.feature.settings.navigation.settingsScreen
import app.dinar.feature.transactions.navigation.transactionsScreen
import app.dinar.core.model.TransactionType
import app.dinar.ui.DinarAppState

@Composable
fun DinarNavHost(appState: DinarAppState, modifier: Modifier = Modifier) {
    val navController: NavHostController = appState.navController
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) {
        homeScreen(
            onAddTransaction = navController::navigateToNewTransaction,
            onTransactionClick = navController::navigateToEditTransaction,
            onSeeAllTransactions = { appState.navigateToTopLevelDestination(TopLevelDestination.TRANSACTIONS) },
            onOpenSettings = navController::navigateToSettings,
            onOpenBudgets = { appState.navigateToTopLevelDestination(TopLevelDestination.BUDGETS) },
        )
        transactionsScreen(
            onTransactionClick = navController::navigateToEditTransaction,
            onAddTransaction = { navController.navigateToNewTransaction(TransactionType.EXPENSE) },
        )
        reportsScreen()
        budgetsScreen()
        editorScreen(onClose = navController::popBackStack)
        settingsScreen(onBack = navController::popBackStack)
    }
}
