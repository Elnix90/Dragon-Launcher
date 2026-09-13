package org.elnix.dragonlauncher.ui.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.BuildTypeChip
import org.elnix.dragonlauncher.ui.components.CodeNameChip
import org.elnix.dragonlauncher.ui.components.VersionCodeChip
import org.elnix.dragonlauncher.ui.components.VersionNumberChip
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.settings.backup.ImportBackupButton
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun WelcomePageIntro(
    isVisible: Boolean,
    setAsSeen: () -> Unit
) {
    val navigator = LocalNavigator.current

    val hasSeenWelcomeOriginal by PrivateSettingsStore.hasSeenWelcome.asState()

    val headlinesAlpha =
        remember(isVisible) {
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
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))

        Image(
            painter = painterResource(R.mipmap.dragon_launcher_foreground),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(260.dp)
        )

        Spacer(32.dp)

        Text(
            stringResource(id = R.string.welcome_to_dragon_launcher),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLargeEmphasized,
            fontSize = 26.sp,
            textAlign = TextAlign.Center
        )

        Spacer(12.dp)

        Text(
            stringResource(id = R.string.app_tagline),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyLargeEmphasized,
            textAlign = TextAlign.Center
        )

        Spacer(20.dp)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            VersionNumberChip()
            CodeNameChip()
            BuildTypeChip()
            VersionCodeChip()
        }

        Spacer(15.dp)

        repeat(3) { i ->
            val text =
                stringResource(
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

        if (hasSeenWelcomeOriginal) {
            val uriHandler = LocalUriHandler.current
            Spacer(30.dp)
            Text(
                text = stringResource(R.string.if_you_see_this_thats_because_i_wanted_to_show_old_users_the_new_welcome_screen),
                style = MaterialTheme.typography.labelMediumEmphasized,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(5.dp)
            Text(
                text = stringResource(R.string.thank_you_for_using_dragon_for_so_long),
                style = MaterialTheme.typography.labelSmallEmphasized,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(5.dp)
            Text(
                text = stringResource(R.string.if_you_click_this_link_youll_get_a_special_role_on_discord),
                style = MaterialTheme.typography.labelSmallEmphasized,
                color = Color.Cyan,
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://discord.gg/Dv84wW3xfD")
                }
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
