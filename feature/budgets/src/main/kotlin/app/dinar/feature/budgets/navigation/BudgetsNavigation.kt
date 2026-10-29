package app.dinar.feature.budgets.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import app.dinar.feature.budgets.BudgetsRoute as BudgetsScreenRoute
import kotlinx.serialization.Serializable

@Serializable
data object BudgetsRoute

fun NavController.navigateToBudgets(navOptions: NavOptions? = null) = navigate(BudgetsRoute, navOptions)

fun NavGraphBuilder.budgetsScreen() {
    composable<BudgetsRoute> { BudgetsScreenRoute() }
}
