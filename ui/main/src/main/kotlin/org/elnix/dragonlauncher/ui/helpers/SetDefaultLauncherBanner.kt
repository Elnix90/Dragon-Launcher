package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.openDefaultLauncherSettings
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow


@Composable
fun SetDefaultLauncherBanner(onHide: () -> Unit) {

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    DragonRow(
        onClick = { ctx.openDefaultLauncherSettings()}
    ) {
        Text(
            stringResource(R.string.set_default_launcher),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.weight(1f)
        )

        DragonIconButton(
            icon = R.drawable.close,
            contentDescription = R.string.close,
            onClick = onHide
        )
    }
}
