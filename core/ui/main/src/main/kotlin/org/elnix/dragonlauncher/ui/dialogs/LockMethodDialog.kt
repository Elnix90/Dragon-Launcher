@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dialogs

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.findFragmentActivity
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.LockScreenViewModel
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

@Suppress("VariableNeverRead")
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun LockMethodDialog(
    lockScreenViewModel: LockScreenViewModel = activityViewModel(),
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current

    val securityService = lockScreenViewModel.securityService

    val currentLockMethod by lockScreenViewModel.lockMethod.collectAsState()
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var pendingLockMethod by remember { mutableStateOf<LockMethod?>(null) }

    if (!showPinSetupDialog) {
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

                        val unavailableText = if (method == LockMethod.DEVICE_UNLOCK && !securityService.isDeviceUnlockAvailable(ctx)) {
                            stringResource(R.string.device_credentials_not_available)
                        } else null


                        fun onClick() {
                            when (method) {
                                LockMethod.PIN -> {
                                    pendingLockMethod = LockMethod.PIN
                                    showPinSetupDialog = true
                                }

                                LockMethod.NONE -> {
                                    lockScreenViewModel.removeLock()
                                    onDismiss()

                                }

                                LockMethod.DEVICE_UNLOCK -> {
                                    // Test biometric authentication immediately
                                    val activity = ctx.findFragmentActivity()
                                    if (activity != null && securityService.isDeviceUnlockAvailable(ctx)) {
                                        securityService.showDeviceUnlockPrompt(
                                            activity = activity,
                                            onSuccess = {
                                                lockScreenViewModel.setLockScreenMethod()
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
    } else {
        PinSetup(
            onDismiss = {
                showPinSetupDialog = false
                pendingLockMethod = null
            },
            onPinSet = { pin ->
                lockScreenViewModel.setPinLockMethod(pin)

                showPinSetupDialog = false
                pendingLockMethod = null
                onDismiss()

            }
        )
    }
}