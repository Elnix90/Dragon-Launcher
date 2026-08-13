package org.elnix.dragonlauncher.ui.dialogs.security

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

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
        CustomAlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    stringResource(R.string.lock_method),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.lock_settings_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                    )

                    Spacer(8.dp)
                    LockMethod.entries.forEach { method ->

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

                        DragonRow(
                            onClick = ::onClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            TextWithDescription(
                                text = stringResource(method.resId),
                                description = unavailableText
                            )

                            Spacer(8.dp)
                            RadioButton(
                                selected = method == currentLockMethod,
                                onClick = ::onClick,
                                colors = AppObjectsColors.radioButtonColors()
                            )
                        }
                    }
                }
            }
        )
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