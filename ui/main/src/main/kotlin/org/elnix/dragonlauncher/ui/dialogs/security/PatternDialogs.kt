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
import io.github.elnix90.lock.PatternLock
import io.github.elnix90.lock.patttern.PatternLockOptions
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.vibrate
import org.elnix.dragonlauncher.models.SecurityViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.animation.slideInVerticalBouncyUp
import org.elnix.dragonlauncher.ui.base.animation.slideOutVerticalBouncyUp
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.dragon.settings.Setting

/**
 * Dialog for entering a PIN to unlock settings.
 */
@Composable
fun PatternUnlock(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    securityViewModel: SecurityViewModel = activityViewModel()
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var failedTries by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val wrongPinText = stringResource(R.string.wrong_pin)

    PatternPrompt(
        title = stringResource(R.string.unlock_settings),
        subtitle = stringResource(R.string.draw_pattern),
        errorMessage = errorMessage,
        showOptionSlider = false,
        failedTries = failedTries,
        onDismiss = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            onDismiss()
        }
    ) { patternString ->
        scope.launch {
            if (securityViewModel.verify(patternString)) {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onSuccess()
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.Reject)
                errorMessage = wrongPinText
                failedTries++
            }
        }
    }
}


/**
 * Dialog for setting up a new PIN (enter + confirm).
 */
@Composable
fun PatternSetup(
    onDismiss: () -> Unit,
    onPattern: (pattern: String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val patternSize by BehaviorSettingsStore.patternSize.asState()
    val doNotRemindMeWarningDialog by UiSettingsStore.doNotRemindMeAgainPinLockWarning.asState()


    var firstPattern by remember { mutableStateOf("") }
    var isConfirmStep by remember { mutableStateOf(false) }

    var showWarningDialog by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedTries by remember { mutableIntStateOf(0) }
    val pinMismatch = stringResource(R.string.pin_mismatch)

    LaunchedEffect(patternSize) {
        errorMessage = null
        firstPattern = ""
    }


    PatternPrompt(
        title = stringResource(R.string.set_pattern),
        subtitle = if (isConfirmStep) stringResource(R.string.confirm_pattern) else stringResource(R.string.draw_pattern),
        errorMessage = errorMessage,
        failedTries = failedTries,
        showOptionSlider = true,
        onDismiss = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            if (isConfirmStep) {
                isConfirmStep = false
                errorMessage = null
            } else {
                onDismiss()
            }
        }
    ) { patternString ->
        if (!isConfirmStep) {
            isConfirmStep = true
            firstPattern = patternString
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        } else {
            when {
                // Error
                firstPattern != patternString -> {
                    errorMessage = pinMismatch
                    failedTries++
                    haptic.performHapticFeedback(HapticFeedbackType.Reject)
                }

                else -> {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)

                    if (doNotRemindMeWarningDialog) {
                        onPattern(firstPattern)
                    } else {
                        showWarningDialog = true
                    }
                }
            }
        }
    }

    if (showWarningDialog) {
        val ctx = LocalContext.current
        val scope = rememberCoroutineScope()

        UserValidation(
            title = stringResource(R.string.pin_code_warning_title),
            message = stringResource(R.string.pin_code_warning_desc),
            doNotRemindMeAgain = {
                scope.launch {
                    UiSettingsStore.doNotRemindMeAgainPinLockWarning.set(ctx, true)
                }
            },
            onDismiss = onDismiss
        ) {
            onPattern(firstPattern)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UseOfNonLambdaOffsetOverload", "MissingPermission")
@Composable
private fun PatternPrompt(
    title: String,
    subtitle: String,
    showOptionSlider: Boolean,
    errorMessage: String? = null,
    failedTries: Int,
    onDismiss: () -> Unit,
    onDrawEnd: (String) -> Unit
) {
    val ctx = LocalContext.current

    val patternSize by BehaviorSettingsStore.patternSize.asState()
    val patternSensitivity by BehaviorSettingsStore.patternSensitivity.asState()
    var showSensitivity by remember { mutableStateOf(false) }

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

    val backgroundOverlayColor = remember {
        Animatable(
            Color.Transparent
        )
    }

    LaunchedEffect(failedTries) {
        if (failedTries > 0 && superWarningMode) {
            while (true) {
                backgroundOverlayColor.animateTo(Color.Red)

                if (vibrateOnError) {
                    // Forcefully vibrate using the low-level API to not rely on the phone settings.
                    // This will ALWAYS vibrate,no matter what the user settings are 😈
                    @Suppress("DEPRECATION")
                    ctx.vibrate(500L)
                }

                backgroundOverlayColor.animateTo(Color.Transparent)
            }
        }
    }

    val defaultLockColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    val lockColor = remember { Animatable(defaultLockColor) }

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

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = slideInVerticalBouncyUp,
                exit = slideOutVerticalBouncyUp
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }


            if (showOptionSlider) {
                DragonSettingsGroup {
                    Setting(BehaviorSettingsStore.patternSize)
                    Setting(BehaviorSettingsStore.patternSensitivity)
                    SwitchRow(
                        state = showSensitivity,
                        title = R.string.show_sensitivity,
                        icon = R.drawable.visibility
                    ) { showSensitivity = it }
                }
            }

            PatternLock(
                modifier = Modifier.padding(bottom = 80.dp),
                patternLockOptions = PatternLockOptions.defaultPatternLockOptions.copy(
                    dimension = patternSize,
                    sensitivity = patternSensitivity,
                    showSensibility = showSensitivity
                ),
                onFinished = onDrawEnd,
            )
        }
    }
}
