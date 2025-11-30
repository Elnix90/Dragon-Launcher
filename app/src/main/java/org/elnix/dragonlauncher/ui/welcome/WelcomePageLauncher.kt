package org.elnix.dragonlauncher.ui.welcome

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.ui.helpers.GradientBigButton

@Composable
fun WelcomePageLauncher() {
    val ctx = LocalContext.current
    val pm = ctx.packageManager

    val isDefaultLauncher by remember {
        androidx.compose.runtime.derivedStateOf {
            isAppDefaultLauncher(pm, ctx.packageName)
        }
    }

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
            text = if (isDefaultLauncher) "Already Default Launcher"
                   else "Open Default Launcher Settings",
            enabled = !isDefaultLauncher,
            onClick = { ctx.startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
        )
    }
}


private fun isAppDefaultLauncher(pm: PackageManager, packageName: String): Boolean {
    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
    }

    // 1. Check if our app resolves as default
    val defaultInfo = pm.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
    if (defaultInfo?.activityInfo?.packageName == packageName) {
        return true
    }

    // 2. Check if our app appears in ALL possible home launchers
    val allLaunchers = pm.queryIntentActivities(homeIntent, 0)
    return allLaunchers.any { it.activityInfo.packageName == packageName }
}
