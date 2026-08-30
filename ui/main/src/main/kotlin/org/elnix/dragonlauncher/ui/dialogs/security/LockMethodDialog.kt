package org.elnix.dragonlauncher.ui.dialogs.security

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod.Device
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.SecurityViewModel
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun LockMethodDialog(
    securityViewModel: SecurityViewModel = activityViewModel(),
    onDismiss: () -> Unit
) {
    val navigator = LocalNavigator.current

    val currentLockMethod by PrivateSettingsStore.lockMethod.asState()

    DragonModalBottomSheet(onDismissRequest = onDismiss) {
        DialogTitle(stringResource(R.string.lock_method))

        Text(
            text = stringResource(R.string.lock_settings_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
        )

        Spacer(8.dp)

        DragonSettingsGroup {
            LockMethod.entries.forEach { method ->
                val selected = method == currentLockMethod

                val unavailableText =
                    if (method == Device && !securityViewModel.isDeviceUnlockAvailable()) {
                        stringResource(R.string.device_credentials_not_available)
                    } else {
                        null
                    }

                val interactionSource = rememberInteractionSource()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier =
                        Modifier
                            .dragonSettingGroup(selected = selected) {
                                clickable(
                                    interactionSource = interactionSource,
                                    onClick = {
                                        onDismiss()
                                        navigator.go(NavigationRoute.LockScreenSetup(method))
                                    }
                                )
                            }.padding(10.dp)
                            .selectableGroup()
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = null,
                        interactionSource = interactionSource
                    )
                    TextWithDescription(
                        text = stringResource(method.resId),
                        description = unavailableText
                    )
                }
            }
        }
    }
}
