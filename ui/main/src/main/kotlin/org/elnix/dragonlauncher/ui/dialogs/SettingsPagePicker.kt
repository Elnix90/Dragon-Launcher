package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.base.navigation.NavigationRoute.Companion.settingsRoutes
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.components.LazyColumnWithScrollIndicator
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPagePicker(
    onDismissRequest: () -> Unit,
    onSelect: (NavigationRoute) -> Unit
) {
    DragonModalBottomSheet(onDismissRequest, true) {
        DialogTitle(stringResource(R.string.pick_a_settings_screen))
        Spacer(5.dp)

        DragonSettingsGroup {
            LazyColumnWithScrollIndicator(
                items = settingsRoutes,
                modifier = Modifier.heightIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) { route ->

                Row(
                    modifier =
                        Modifier.dragonSettingGroup {
                            clickable { onSelect(route) }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(route.icon),
                        contentDescription = null
                    )

                    Text(
                        text = stringResource(route.resId),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
