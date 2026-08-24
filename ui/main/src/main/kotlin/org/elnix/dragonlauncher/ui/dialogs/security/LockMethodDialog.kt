package org.elnix.dragonlauncher.ui.dialogs.security

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod.Device
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod.None
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod.Pattern
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod.Pin
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.findFragmentActivity
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.SecurityViewModel
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
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
    val ctx = LocalContext.current

    val currentLockMethod by PrivateSettingsStore.lockMethod.asState()
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showPatternSetupDialog by remember { mutableStateOf(false) }
    var pendingLockMethod by remember { mutableStateOf<LockMethod?>(null) }

    if (!showPinSetupDialog && !showPatternSetupDialog) {
        DragonModalBottomSheet(onDismissRequest = onDismiss) {
            DialogTitle(stringResource(R.string.lock_method))

            Text(
                text = stringResource(R.string.lock_settings_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
            )

            Spacer(8.dp)

            DragonSettingsGroup {
                val primaryContainer = MaterialTheme.colorScheme.primaryContainer

                LockMethod.entries.forEach { method ->
                    val selected = method == currentLockMethod

                    val unavailableText = if (method == Device && !securityViewModel.isDeviceUnlockAvailable()) {
                        stringResource(R.string.device_credentials_not_available)
                    } else null


                    fun onClick() {
                        when (method) {
                            Pin -> {
                                pendingLockMethod = Pin
                                showPinSetupDialog = true
                            }

                            Pattern -> {
                                pendingLockMethod = Pattern
                                showPatternSetupDialog = true
                            }

                            None -> {
                                securityViewModel.removeLock()
                                onDismiss()

                            }

                            Device -> {
                                // Test biometric authentication immediately
                                val activity = ctx.findFragmentActivity()
                                if (activity != null && securityViewModel.isDeviceUnlockAvailable()) {
                                    securityViewModel.showDeviceUnlockPrompt(
                                        activity = activity,
                                        onSuccess = {
                                            securityViewModel.setLockScreenMethod()
                                            onDismiss()
                                        },
                                        onError = { msg ->
                                            ctx.showToast(ctx.getString(R.string.authentication_error, msg))
                                        },
                                        onFailed = {
                                            ctx.showToast(ctx.getString(R.string.authentication_failed))
                                        }
                                    )
                                } else {
                                    ctx.showToast(ctx.getString(R.string.device_credentials_not_available))
                                }
                            }
                        }
                    }

                    val interactionSource = rememberInteractionSource()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .dragonSettingGroup {
                                clickable(
                                    interactionSource = interactionSource,
                                    onClick = ::onClick
                                )
                                    .conditional(selected) {
                                        background(primaryContainer)
                                    }
                            }
                            .padding(10.dp)

                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = null,
                            interactionSource = interactionSource,
                        )
                        TextWithDescription(
                            text = stringResource(method.resId),
                            description = unavailableText,
                        )
                    }
                }
            }
        }
    }


    if (showPatternSetupDialog) {
        PatternSetup(
            onDismiss = {
                showPatternSetupDialog = false
                pendingLockMethod = null
            },
            onPinSet = { pin ->
                securityViewModel.setPatternLockMethod(pin)

                showPatternSetupDialog = false
                pendingLockMethod = null
                onDismiss()
            }
        )
    }

    if (showPinSetupDialog) {
        PinSetup(
            onDismiss = {
                showPinSetupDialog = false
                pendingLockMethod = null
            },
            onPinSet = { pin ->
                securityViewModel.setPinLockMethod(pin)

                showPinSetupDialog = false
                pendingLockMethod = null
                onDismiss()
            }
        )
    }
}