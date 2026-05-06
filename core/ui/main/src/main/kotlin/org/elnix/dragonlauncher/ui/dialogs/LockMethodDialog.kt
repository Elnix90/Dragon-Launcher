@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dialogs

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import org.elnix.dragonlauncher.ui.helpers.SecurityHelper
import org.elnix.dragonlauncher.ui.helpers.findFragmentActivity

@Suppress("VariableNeverRead")
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun LockMethodDialog(
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentLockMethod by PrivateSettingsStore.lockMethod.asState()
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var pendingLockMethod by remember { mutableStateOf<LockMethod?>(null) }

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

                Spacer(Modifier.height(8.dp))
                LockMethod.entries.forEach { method ->

                    val unavailableText = if (method == LockMethod.DEVICE_UNLOCK && !SecurityHelper.isDeviceUnlockAvailable(ctx)) {
                        stringResource(R.string.device_credentials_not_available)
                    } else null


                    fun onClick() {
                        when (method) {
                            LockMethod.PIN -> {
                                pendingLockMethod = LockMethod.PIN
                                showPinSetupDialog = true
                            }

                            LockMethod.NONE -> {
                                scope.launch {
                                    PrivateSettingsStore.lockPinHash.reset(ctx)
                                    PrivateSettingsStore.lockMethod.reset(ctx)
                                    onDismiss()
                                }
                            }

                            LockMethod.DEVICE_UNLOCK -> {
                                // Test biometric authentication immediately
                                val activity = ctx.findFragmentActivity()
                                if (activity != null && SecurityHelper.isDeviceUnlockAvailable(ctx)) {
                                    SecurityHelper.showDeviceUnlockPrompt(
                                        activity = activity,
                                        onSuccess = {
                                            scope.launch {
                                                PrivateSettingsStore.lockPinHash.reset(ctx)
                                                PrivateSettingsStore.lockMethod.set(ctx, LockMethod.DEVICE_UNLOCK)
                                                onDismiss()
                                            }
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

                        Spacer(Modifier.width(8.dp))
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

    // ── PIN setup dialog ──
    if (showPinSetupDialog) {
        PinSetup(
            onDismiss = {
                showPinSetupDialog = false
                pendingLockMethod = null
            },
            onPinSet = { pin ->
                scope.launch {
                    val hash = SecurityHelper.hashPin(pin)
                    PrivateSettingsStore.lockPinHash.set(ctx, hash)
                    PrivateSettingsStore.lockMethod.set(ctx, LockMethod.PIN)
                    ctx.showToast(ctx.getString(R.string.pin_set_success))

                    showPinSetupDialog = false
                    pendingLockMethod = null
                    onDismiss()
                }
            }
        )
    }
}