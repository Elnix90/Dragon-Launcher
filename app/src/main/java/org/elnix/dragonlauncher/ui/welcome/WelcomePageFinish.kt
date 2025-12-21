package org.elnix.dragonlauncher.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.ui.helpers.GradientBigButton

@Composable
fun WelcomePageFinish(
    onEnterSettings: () -> Unit,
    onEnterApp: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.celebration),
                contentDescription = stringResource(R.string.everything_ready),
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = stringResource(R.string.everything_ready),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp
            )
        }

        Spacer(Modifier.height(32.dp))

        GradientBigButton(
            text = "Customize applications",
            onClick = onEnterSettings
        )


        Spacer(Modifier.weight(1f))


        TextButton(onClick = onEnterApp) {
            Text(
                text = "Don't customize and start using directly Dragon Launcher",
                color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                textDecoration = TextDecoration.Underline
            )
        }
    }
}
