package org.elnix.dragonlauncher.ui.dialogs.security

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.lock.PinLock
import io.github.elnix90.runtime.asMutableState
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.vibrate
import org.elnix.dragonlauncher.models.SecurityViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation

/**
 * Dialog for entering a PIN to unlock settings.
 */
@Composable
fun PinUnlock(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    securityViewModel: SecurityViewModel = activityViewModel()
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var failedTries by remember { mutableIntStateOf(0) }
    var pinErrorMessage by remember { mutableStateOf<String?>(null) }
    val wrongPinText = stringResource(R.string.wrong_pin)

    PinPrompt(
        title = stringResource(R.string.unlock_settings),
        subtitle = stringResource(R.string.enter_pin),
        errorMessage = pinErrorMessage,
        failedTries = failedTries,
        onDismiss = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            onDismiss()
        }
    ) { pin ->
        scope.launch {
            if (securityViewModel.verify(pin)) {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onSuccess()
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.Reject)
                pinErrorMessage = wrongPinText
                failedTries++
            }
        }
    }
}


/**
 * Dialog for setting up a new PIN (enter + confirm).
 */
@Composable
fun PinSetup(
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var firstPin by remember { mutableStateOf("") }
    var isConfirmStep by remember { mutableStateOf(false) }

    var showWarningDialog by remember { mutableStateOf(false) }
    var doNotRemindMeWarningDialog by UiSettingsStore.doNotRemindMeAgainPinLockWarning.asMutableState()

    var pinErrorMessage by remember { mutableStateOf<String?>(null) }
    var failedTries by remember { mutableIntStateOf(0) }
    val pinMismatchText = stringResource(R.string.pin_mismatch)

    PinPrompt(
        title = stringResource(R.string.set_pin),
        subtitle = if (isConfirmStep) stringResource(R.string.confirm_pin) else stringResource(R.string.enter_pin),
        errorMessage = pinErrorMessage,
        failedTries = failedTries,
        onDismiss = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            if (isConfirmStep) {
                isConfirmStep = false
                pinErrorMessage = null
            } else {
                onDismiss()
            }
        },
        onValidate = { pin ->
            if (isConfirmStep) {
                when {
                    // Error
                    firstPin != pin -> {
                        pinErrorMessage = pinMismatchText
                        failedTries++
                        haptic.performHapticFeedback(HapticFeedbackType.Reject)
                    }

                    else -> {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)

                        if (doNotRemindMeWarningDialog) {
                            onPinSet(firstPin)
                        } else {
                            showWarningDialog = true
                        }
                    }
                }
            } else {
                isConfirmStep = true
                firstPin = pin
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            }
        }
    )

    if (showWarningDialog) {
        UserValidation(
            title = stringResource(R.string.pin_code_warning_title),
            message = stringResource(R.string.pin_code_warning_desc),
            doNotRemindMeAgain = { doNotRemindMeWarningDialog = it },
            onDismiss = onDismiss,
            onValidate = { onPinSet(firstPin) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
private fun PinPrompt(
    title: String,
    subtitle: String,
    errorMessage: String? = null,
    failedTries: Int,
    onDismiss: () -> Unit,
    onValidate: (pin: String) -> Unit
) {
    val ctx = LocalContext.current
    val horizontalOffsetError = remember {
        Animatable(
            initialValue = 0f
        )
    }

    LaunchedEffect(failedTries) {
        if (failedTries > 0) {
            var left = true
            repeat(5) {
                horizontalOffsetError.animateTo(
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearEasing
                    ),
                    targetValue = if (left) -5f
                    else 5f
                )
                left = !left
            }
            horizontalOffsetError.animateTo(0f)
        }
    }

    val superWarningMode by BehaviorSettingsStore.superWarningMode.asState()
    val superWarningModeSound by BehaviorSettingsStore.superWarningModeSound.asState()
    val vibrateOnError by BehaviorSettingsStore.vibrateOnError.asState()
    val alarmSound by BehaviorSettingsStore.alarmSound.asState()
    val metalPipesSound by BehaviorSettingsStore.metalPipesSound.asState()

    PlayWarningSounds(
        failedTries = failedTries,
        superWarningMode = superWarningMode,
        superWarningModeSound = superWarningModeSound,
        alarmSoundEnabled = alarmSound,
        metalPipesSoundEnabled = metalPipesSound
    )

    val backgroundOverlayColor = remember { Animatable(Color.Transparent) }

    LaunchedEffect(failedTries) {
        if (failedTries > 0 && superWarningMode) {
            while (true) {
                backgroundOverlayColor.animateTo(Color.Red)

                if (vibrateOnError) {
                    // Forcefully vibrate using the low-level API to not rely on the phone settings.
                    // This will ALWAYS vibrate,no matter what the user settings are 😈
                    // EDIT: since a while, it doesn't work anymore, but I still need it to play a 500 milliseconds haptic repetitively
                    @Suppress("DEPRECATION")
                    ctx.vibrate(500L)
                }

                backgroundOverlayColor.animateTo(Color.Transparent)
            }
        }
    }

    // Lock color animation system
    val defaultLockColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    val lockColor = remember {
        Animatable(
            initialValue = defaultLockColor
        )
    }

    LaunchedEffect(failedTries) {
        if (failedTries > 0) {
            lockColor.animateTo(errorColor)
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage == null) {
            lockColor.animateTo(defaultLockColor)
        }
    }

    BackHandler(onBack = onDismiss)

    LockScreenScaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundOverlayColor.value)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Icon(
                painter = painterResource(R.drawable.lock),
                contentDescription = null,
                tint = lockColor.value,
                modifier = Modifier
                    .offset(x = horizontalOffsetError.value.dp)
                    .size(50.dp)
            )
            Spacer(8.dp)

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            AnimatedVisibility(errorMessage != null) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(40.dp)
            PinLock(
                modifier = Modifier.padding(bottom = 80.dp),
                onValidate = onValidate
            )
        }
    }
}
