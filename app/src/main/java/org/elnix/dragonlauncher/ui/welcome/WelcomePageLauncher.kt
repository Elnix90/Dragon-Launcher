package org.elnix.dragonlauncher.ui.welcome

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.R
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ){
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = stringResource(R.string.set_default_launcher),
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.set_default_launcher),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp
            )
        }


        Spacer(Modifier.height(32.dp))

        GradientBigButton(
            text = if (isDefaultLauncher)
                "Already Default Launcher"
            else
                "Open Default Launcher Settings",
            enabled = !isDefaultLauncher,
            onClick = {
                ctx.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            }
        )
    }
}
