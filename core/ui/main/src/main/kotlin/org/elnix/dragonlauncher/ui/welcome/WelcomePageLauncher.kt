package org.elnix.dragonlauncher.ui.welcome

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.utils.rememberIsDefaultLauncher
import org.elnix.dragonlauncher.ui.helpers.GradientBigButton

@Composable
fun WelcomePageLauncher() {
    val ctx = LocalContext.current
    val isDefaultLauncher = rememberIsDefaultLauncher()

   WelcomePagerHeader(
       title = stringResource(R.string.set_default_launcher),
       icon = R.drawable.rocket_launch
   ) {
        GradientBigButton(
            text = if (isDefaultLauncher)
                stringResource(R.string.already_default_launcher)
            else
                stringResource(R.string.open_default_launcher_settings),
            enabled = !isDefaultLauncher,
            onClick = {
                ctx.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            }
        )
    }
}
