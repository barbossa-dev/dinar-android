package app.dinar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dinar.core.designsystem.theme.DinarTheme
import app.dinar.core.model.ThemeConfig
import app.dinar.core.model.TransactionType
import app.dinar.core.ui.DisplaySettings
import app.dinar.core.ui.LocalDisplaySettings
import app.dinar.ui.DinarApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    /** Set from launcher shortcuts; consumed once by the UI. */
    private var pendingAction by mutableStateOf<TransactionType?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { viewModel.uiState.value is MainActivityUiState.Loading }
        pendingAction = intent.toShortcutAction()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val prefs = (state as? MainActivityUiState.Success)?.preferences ?: return@setContent
            val darkTheme = when (prefs.themeConfig) {
                ThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                ThemeConfig.LIGHT -> false
                ThemeConfig.DARK -> true
            }

            // Keep system bar icons readable for the chosen theme, not the system one.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LightScrim, DarkScrim) { darkTheme },
                )
                onDispose {}
            }

            CompositionLocalProvider(LocalDisplaySettings provides DisplaySettings.from(prefs)) {
                DinarTheme(darkTheme = darkTheme, dynamicColor = prefs.useDynamicColor) {
                    DinarApp(
                        pendingAction = pendingAction,
                        onPendingActionHandled = { pendingAction = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingAction = intent.toShortcutAction()
    }

    private fun Intent?.toShortcutAction(): TransactionType? = when (this?.action) {
        ACTION_ADD_EXPENSE -> TransactionType.EXPENSE
        ACTION_ADD_INCOME -> TransactionType.INCOME
        else -> null
    }

    companion object {
        const val ACTION_ADD_EXPENSE = "app.dinar.action.ADD_EXPENSE"
        const val ACTION_ADD_INCOME = "app.dinar.action.ADD_INCOME"
    }
}

private val LightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DarkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
