package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Companion.settingsRoutes
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Settings.routeResId
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.components.LazyColumnWithScrollIndicator
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog

@Composable
fun SettingsPagePicker(
    onDismissRequest: () -> Unit,
    onSelect: (NavigationRoute) -> Unit
) {
    CustomAlertDialog(
        modifier = Modifier.padding(40.dp),
        onDismissRequest = onDismissRequest,
        alignment = Alignment.Center,
        scroll = false,
        title = {
            Text(
                text = stringResource(R.string.pick_a_settings_screen),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            LazyColumnWithScrollIndicator(
                items = settingsRoutes,
                modifier = Modifier.heightIn(max = 500.dp)
            ) { route ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(DragonShape)
                        .clickable { onSelect(route) }
                        .padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    RadioButton(
                        selected = false,
                        onClick = null
                    )

                    Text(
                        text = stringResource(routeResId(route)),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}
