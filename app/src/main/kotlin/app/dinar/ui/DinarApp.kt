package app.dinar.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import app.dinar.core.model.TransactionType
import app.dinar.feature.editor.navigation.navigateToNewTransaction
import app.dinar.navigation.DinarNavHost
import app.dinar.navigation.TopLevelDestination

/**
 * App shell: an adaptive navigation suite (bottom bar on phones, rail on tablets and
 * foldables) that hides itself on full-screen flows like the editor and settings.
 */
@Composable
fun DinarApp(
    pendingAction: TransactionType?,
    onPendingActionHandled: () -> Unit,
    appState: DinarAppState = rememberDinarAppState(),
) {
    val current = appState.currentTopLevelDestination

    LaunchedEffect(pendingAction) {
        if (pendingAction != null) {
            appState.navController.navigateToNewTransaction(pendingAction)
            onPendingActionHandled()
        }
    }

    val layoutType = if (current == null) {
        NavigationSuiteType.None
    } else {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    }

    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                val selected = destination == current
                item(
                    selected = selected,
                    onClick = { appState.navigateToTopLevelDestination(destination) },
                    icon = {
                        Icon(
                            if (selected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(destination.label) },
                )
            }
        },
    ) {
        DinarNavHost(appState = appState, modifier = Modifier.fillMaxSize())
    }
}
