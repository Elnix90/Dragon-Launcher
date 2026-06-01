package org.elnix.dragonlauncher.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.elnix.dragonlauncher.theme.DragonLauncherTheme
import org.elnix.dragonlauncher.ui.wellbeing.TimeLimitExceededScreen

/**
 * Beautiful full-screen activity shown when the user's time limit on a paused app
 * has been exceeded. Brings them back to Dragon Launcher with a calming message.
 */
class TimeLimitExceededActivity : ComponentActivity() {

    companion object {
        const val EXTRA_APP_NAME = "extra_app_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appName = intent.getStringExtra(EXTRA_APP_NAME)
            ?: applicationInfo.loadLabel(packageManager).toString()

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            DragonLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0E21)
                ) {
                    TimeLimitExceededScreen(
                        appName = appName,
                        onDismiss = { finish() }
                    )
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }
}

