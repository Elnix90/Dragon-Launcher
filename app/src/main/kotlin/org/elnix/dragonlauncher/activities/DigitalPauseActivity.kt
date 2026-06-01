package org.elnix.dragonlauncher.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.elnix.dragonlauncher.theme.DragonLauncherTheme
import org.elnix.dragonlauncher.ui.wellbeing.DigitalPauseScreen

class DigitalPauseActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_PAUSE_DURATION = "extra_pause_duration"
        const val EXTRA_GUILT_MODE = "extra_guilt_mode"

        // New: Reminder mode
        const val EXTRA_REMINDER_ENABLED = "extra_reminder_enabled"
        const val EXTRA_REMINDER_INTERVAL = "extra_reminder_interval"
        const val EXTRA_REMINDER_MODE = "extra_reminder_mode"

        // Return-to-launcher mode
        const val EXTRA_RETURN_TO_LAUNCHER = "extra_return_to_launcher"

        const val RESULT_PROCEED = 1
        const val RESULT_PROCEED_WITH_TIMER = 2
        const val RESULT_CANCEL = 0

        const val RESULT_EXTRA_TIME_LIMIT = "result_time_limit_minutes"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: run {
            finish()
            return
        }
        val pauseDuration = intent.getIntExtra(EXTRA_PAUSE_DURATION, 10)
        val guiltMode = intent.getBooleanExtra(EXTRA_GUILT_MODE, false)
        val returnToLauncher = intent.getBooleanExtra(EXTRA_RETURN_TO_LAUNCHER, false)
        val reminderEnabled = intent.getBooleanExtra(EXTRA_REMINDER_ENABLED, false)
        val reminderInterval = intent.getIntExtra(EXTRA_REMINDER_INTERVAL, 5)
        val reminderMode = intent.getStringExtra(EXTRA_REMINDER_MODE) ?: "overlay"

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            BackHandler {
                setResult(RESULT_CANCEL)
            }

            DragonLauncherTheme {


                }
            }
        }
    }
}
