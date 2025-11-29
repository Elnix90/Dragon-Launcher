package org.elnix.dragonlauncher

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.elnix.dragonlauncher.ui.MainScreen
import org.elnix.dragonlauncher.ui.theme.DragonLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // Use hardware acceleration
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navigateToSettings = remember { mutableStateOf(false) }
            val navigateToAppDrawer = remember { mutableStateOf(false) }


            LaunchedEffect(navigateToSettings.value) {
                if (navigateToSettings.value) {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    navigateToSettings.value = false
                }
            }

            LaunchedEffect(navigateToAppDrawer.value) {
                if (navigateToAppDrawer.value) {
                    startActivity(Intent(this@MainActivity, AppDrawerActivity::class.java))
                    navigateToAppDrawer.value = false
                }
            }

            DragonLauncherTheme {
                MainScreen(
                    onAppDrawer = {
                        navigateToAppDrawer.value = true
                    },
                    onLongPress3Sec = {
                        navigateToSettings.value = true
                    }
                )
            }
        }
    }
}
