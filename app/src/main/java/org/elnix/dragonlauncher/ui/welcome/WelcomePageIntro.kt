package org.elnix.dragonlauncher.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.R

@Composable
fun WelcomePageIntro(onImport: () -> Unit) {

    val ctx = LocalContext.current
    val versionName = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "unknown"

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))

        Image(
            painter = painterResource(R.drawable.dragon_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier.size(260.dp)
        )

        Spacer(Modifier.height(32.dp))

        Text(
            "Welcome to Dragon Launcher",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 26.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "${stringResource(R.string.version)} $versionName",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Fast. Minimal. Powerful gestures.",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 18.sp
        )

        Spacer(Modifier.weight(1f))

        TextButton(
            onClick = onImport
        ) {
            Text(
                text = "Import settings",
                color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                textDecoration = TextDecoration.Underline
            )
        }
    }
}
