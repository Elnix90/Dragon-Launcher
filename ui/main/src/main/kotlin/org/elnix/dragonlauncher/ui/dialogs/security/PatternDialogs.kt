@file:Suppress("AssignedValueIsNeverRead")

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import io.github.elnix90.lock.ComposeLock
import io.github.elnix90.lock.ComposeLockCallback
import io.github.elnix90.lock.Dot
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.vibrate
import org.elnix.dragonlauncher.models.SecurityViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.animation.slideInVerticalBouncyUp
import org.elnix.dragonlauncher.ui.base.animation.slideOutVerticalBouncyUp
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.modifiers.selfAlignHorizontally
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.dragon.text.DialogDescription
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle

/**
 * Dialog for entering a PIN to unlock settings.
 */
@Composable
fun PatternUnlock(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    securityViewModel: SecurityViewModel = activityViewModel()
) {
    val haptic = LocalHapticFeedback.current
    val pinHash by PrivateSettingsStore.lockHash.asState()

    var pattern by remember { mutableStateOf("") }
    var failedTries by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val wrongPinText = stringResource(R.string.wrong_pin)

    PatternPrompt(
        title = stringResource(R.string.unlock_settings),
        subtitle = stringResource(R.string.draw_pattern),
        patternValue = pattern,
        errorMessage = errorMessage,
        showSizeSlider = false,
        failedTries = failedTries,
        onAddPoint = { newDotId ->
            errorMessage = null
            pattern += newDotId
        },
        onDismiss = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            onDismiss()
        }
    ) {
        if (securityViewModel.verify(pattern, pinHash)) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            pattern = ""
            onSuccess()
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
            errorMessage = wrongPinText
            failedTries++
            pattern = ""
        }
    }
}


/**
 * Dialog for setting up a new PIN (enter + confirm).
 */
@Composable
fun PatternSetup(
    onDismiss: () -> Unit,
    onPinSet: (pattern: String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val patternSize by PrivateSettingsStore.patternSize.asState()
    val doNotRemindMeWarningDialog by UiSettingsStore.doNotRemindMeAgainPinLockWarning.asState()


    var firstPattern by remember { mutableStateOf("") }
    var confirmPattern by remember { mutableStateOf("") }
    var isConfirmStep by remember { mutableStateOf(false) }

    var showWarningDialog by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedTries by remember { mutableIntStateOf(0) }
    val pinMismatch = stringResource(R.string.pin_mismatch)

    val currentPin = if (isConfirmStep) confirmPattern else firstPattern

    LaunchedEffect(patternSize) {
        errorMessage = null
        firstPattern = ""
        confirmPattern = ""
    }


    PatternPrompt(
        title = stringResource(R.string.set_pattern),
        subtitle = if (isConfirmStep) stringResource(R.string.confirm_pattern) else stringResource(R.string.draw_pattern),
        patternValue = currentPin,
        errorMessage = errorMessage,
        failedTries = failedTries,
        showSizeSlider = true,
        onAddPoint = { newDotId ->
            errorMessage = null
            if (isConfirmStep) {
                confirmPattern += newDotId
            } else {
                firstPattern += newDotId
            }
        },
        onDismiss = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            if (isConfirmStep) {
                isConfirmStep = false
                confirmPattern = ""
                errorMessage = null
            } else {
                onDismiss()
            }
        }
    ) {
        if (!isConfirmStep) {
            isConfirmStep = true
            confirmPattern = ""
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        } else {
            when {
                // Error
                firstPattern != confirmPattern -> {
                    errorMessage = pinMismatch
                    confirmPattern = ""
                    failedTries++
                    haptic.performHapticFeedback(HapticFeedbackType.Reject)
                }

                else -> {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)

                    if (doNotRemindMeWarningDialog) {
                        onPinSet(firstPattern)
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
            title = stringResource(R.string.pin_code_warning_titls),
            message = stringResource(R.string.pin_code_warning_desc),
            doNotRemindMeAgain = {
                scope.launch {
                    UiSettingsStore.doNotRemindMeAgainPinLockWarning.set(ctx, true)
                }
            },
            onDismiss = onDismiss
        ) {
            onPinSet(firstPattern)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UseOfNonLambdaOffsetOverload", "MissingPermission")
@Composable
private fun PatternPrompt(
    title: String,
    subtitle: String,
    patternValue: String,
    showSizeSlider: Boolean,
    errorMessage: String? = null,
    failedTries: Int,
    onAddPoint: (String) -> Unit,
    onDismiss: () -> Unit,
    onDrawEnd: () -> Unit
) {
    val ctx = LocalContext.current
    val patternSize by PrivateSettingsStore.patternSize.asState()

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBarsIgnoringVisibility
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(backgroundOverlayColor.value)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Icon(
                    painter = painterResource(R.drawable.lock),
                    contentDescription = null,
                    tint = lockColor.value,
                    modifier = Modifier
                        .offset(x = horizontalOffsetError.value.dp)
                        .size(34.dp)
                )

                DialogTitle(
                    text = title,
                    modifier = Modifier.selfAlignHorizontally()
                )
                DialogDescription(subtitle)

                if (showSizeSlider) DragonSettingsGroup { Setting(PrivateSettingsStore.patternSize) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                }
            }

            ComposeLock(
                dimension = patternSize,
                sensitivity = 100f,
                dotsColor = MaterialTheme.colorScheme.primary,
                dotsSize = 20f,
                linesColor = MaterialTheme.colorScheme.secondary,
                linesStroke = 30f,
                animationDuration = 200,
                animationDelay = 100,
                callback = object : ComposeLockCallback {
                    override fun onDotConnected(dot: Dot) {
                        onAddPoint(dot.id.toString())
                    }

                    override fun onResult(result: List<Dot>) {
                        onDrawEnd()
                    }

                    override fun onStart(dot: Dot) {
                        onAddPoint(dot.id.toString())
                    }
                }
            )


            AnimatedFab(
                icon = R.drawable.close,
                minSize = 100.dp,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                onClick = onDismiss
            )
        }
    }
}
