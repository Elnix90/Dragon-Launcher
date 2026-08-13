package org.elnix.dragonlauncher.ui.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.settings.backup.ImportBackupButton
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun WelcomePageIntro(
    isVisible: Boolean,
    setAsSeen: () -> Unit
) {
    val ctx = LocalContext.current
    val navigator = LocalNavigator.current

    val versionName = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "unknown"

    val headlinesAlpha = remember(isVisible) {
        List(3) { Animatable(initialValue = 0f) }
    }

    LaunchedEffect(isVisible) {
        delay(500.milliseconds)
        for (i in 0..2) {
            headlinesAlpha[i].animateTo(
                targetValue = 1f,
                animationSpec = tween(750)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))

        Image(
            painter = painterResource(R.mipmap.dragon_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier.size(260.dp)
        )

        Spacer(32.dp)

        Text(
            stringResource(R.string.welcome_to_dragon_launcher),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 26.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.version, versionName),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic
        )

        Spacer(12.dp)

        Text(
            stringResource(R.string.dragon_launcher_headline),
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )

        Spacer(15.dp)

        repeat(3) { i ->
            val text = stringResource(
                when (i) {
                    0 -> R.string.fast
                    1 -> R.string.powerful_gestures
                    else -> R.string.infinite_custom
                }
            )

            Text(
                text = text,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = headlinesAlpha[i].value),
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.weight(1f))

        ImportBackupButton(
            onConfirm = {
                setAsSeen()
                // Here I do not check the initialization of the launcher, as th user imports it's settings, and therefore, it is initialized!
                navigator.onBack()
            }
        ) {
            TextButton(
                onClick = it
            ) {
                Text(
                    text = stringResource(R.string.import_settings),
                    color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                    textDecoration = TextDecoration.Underline,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
