package org.elnix.dragonlauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.elnix.dragonlauncher.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navigateToSettings = remember { mutableStateOf(false) }

            LaunchedEffect(navigateToSettings.value) {
                if (navigateToSettings.value) {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    navigateToSettings.value = false
                }
            }

            MainScreen(
                onLongPress3Sec = {
                    navigateToSettings.value = true
                }
            )
        }
    }
}
