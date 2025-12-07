package org.elnix.dragonlauncher.ui.welcome

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.ui.helpers.GradientBigButton
import org.elnix.dragonlauncher.utils.isDefaultLauncher

@Composable
fun WelcomePageLauncher() {
    val ctx = LocalContext.current

    val isDefaultLauncher = ctx.isDefaultLauncher

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Set as Default Launcher",
            color = Color.White,
            fontSize = 24.sp
        )

        Spacer(Modifier.height(32.dp))

        GradientBigButton(
            text = if (isDefaultLauncher)
                "Already Default Launcher"
            else
                "Open Default Launcher Settings",
            enabled = !isDefaultLauncher,
            onClick = {
                ctx.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))

//                // Re-check when user comes back
//                // Using a slight delay avoids checking too early
//                android.os.Handler(android.os.Looper.getMainLooper())
//                    .postDelayed({
//                        isDefaultLauncher.value = ctx.isDefaultLauncher
//                    }, 500)
            }
        )
    }
}
