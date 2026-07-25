package org.elnix.dragonlauncher.ui.helpers.workspace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.openDefaultLauncherSettings
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton

@Composable
public fun WorkspaceUnavailableContent(
    workspaceType: WorkspaceType
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {

        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(workspaceType.icon),
                    contentDescription = "Workspace icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(10.dp)

                Text(
                    text = stringResource(R.string.no_profile_found),
                    style = MaterialTheme.typography.bodyLargeEmphasized
                )

                if (workspaceType == WorkspaceType.Work || workspaceType == WorkspaceType.Private) {
                    val ctx = LocalContext.current
                    Spacer(5.dp)

                    Text(
                        text = stringResource(R.string.need_to_be_default_launcher_to_access_profile),
                        style = MaterialTheme.typography.labelMedium
                    )

                    DragonButton(
                        onClick = {
                            ctx.openDefaultLauncherSettings()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.set_default_launcher),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}