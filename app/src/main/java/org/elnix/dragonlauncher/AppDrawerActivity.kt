package org.elnix.dragonlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import org.elnix.dragonlauncher.ui.drawer.AppDrawerScreen
import org.elnix.dragonlauncher.ui.theme.DragonLauncherTheme
import org.elnix.dragonlauncher.utils.AppDrawerViewModel


class AppDrawerActivity : ComponentActivity() {
    private val viewModel : AppDrawerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DragonLauncherTheme {
                AppDrawerScreen(
                    viewModel = viewModel,
                    showIcons = true,
                    onClose = { finish() }
                )
            }
        }
    }
}