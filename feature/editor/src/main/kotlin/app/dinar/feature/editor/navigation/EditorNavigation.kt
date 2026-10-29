package app.dinar.feature.editor.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.dinar.core.model.TransactionType
import app.dinar.feature.editor.EditorRoute as EditorScreenRoute
import kotlinx.serialization.Serializable

/** `transactionId == 0` creates a new transaction of [type]. */
@Serializable
data class EditorRoute(
    val transactionId: Long = 0L,
    val type: String = TransactionType.EXPENSE.name,
) {
    val transactionType: TransactionType
        get() = TransactionType.entries.firstOrNull { it.name == type } ?: TransactionType.EXPENSE
}

fun NavController.navigateToNewTransaction(type: TransactionType) =
    navigate(EditorRoute(type = type.name)) { launchSingleTop = true }

fun NavController.navigateToEditTransaction(id: Long) =
    navigate(EditorRoute(transactionId = id)) { launchSingleTop = true }

fun NavGraphBuilder.editorScreen(onClose: () -> Unit) {
    composable<EditorRoute> {
        EditorScreenRoute(onClose = onClose)
    }
}
