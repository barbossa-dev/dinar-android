package app.dinar.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import app.dinar.core.designsystem.icon.DinarIcons
import app.dinar.feature.budgets.navigation.BudgetsRoute
import app.dinar.feature.home.navigation.HomeRoute
import app.dinar.feature.reports.navigation.ReportsRoute
import app.dinar.feature.transactions.navigation.TransactionsRoute
import kotlin.reflect.KClass

/** Destinations reachable from the navigation bar / rail. */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val route: KClass<*>,
) {
    HOME(DinarIcons.Home, DinarIcons.HomeOutlined, "خانه", HomeRoute::class),
    TRANSACTIONS(DinarIcons.Transactions, DinarIcons.Transactions, "تراکنش‌ها", TransactionsRoute::class),
    REPORTS(DinarIcons.Reports, DinarIcons.ReportsOutlined, "گزارش", ReportsRoute::class),
    BUDGETS(DinarIcons.Budget, DinarIcons.BudgetOutlined, "بودجه", BudgetsRoute::class),
}
