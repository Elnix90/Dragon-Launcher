@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.util.ColorUtils.semiTransparentIfDisabled
import org.elnix.dragonlauncher.base.util.HapticUtils.vibrate
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.LockScreenViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants.pinMaterialShapes
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation

/**
 * Dialog for entering a PIN to unlock settings.
 */
@Composable
public fun PinUnlock(
    onDismiss: () -> Unit,
    onValidate: () -> Unit,
    lockScreenViewModel: LockScreenViewModel = activityViewModel()
) {
    val haptic = LocalHapticFeedback.current
    val pinHash by PrivateSettingsStore.lockPinHash.asState()

    var pin by remember { mutableStateOf("") }
    val pinShapes = remember { mutableStateListOf<RoundedPolygon>() }
    var failedTries by remember { mutableIntStateOf(0) }
    var pinError by remember { mutableStateOf<String?>(null) }

    val wrongPinText = stringResource(R.string.wrong_pin)

    PinPrompt(
        title = stringResource(R.string.unlock_settings),
        subtitle = stringResource(R.string.enter_pin),
        pinValue = pin,
        pinShapes = pinShapes,
        errorMessage = pinError,
        failedTries = failedTries,
        onPinChanged = { newValue ->
            pinError = null
            pin = newValue
            if (pinShapes.size < newValue.length) {
                repeat(newValue.length - pinShapes.size) {
                    pinShapes.add(pinMaterialShapes.random())
                }
            } else {
                repeat(pinShapes.size - newValue.length) {
                    pinShapes.removeAt(pinShapes.lastIndex)
                }
            }
        },
        onDismiss = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            onDismiss()
        }
    ) {
        if (lockScreenViewModel.verifyPin(pin, pinHash)) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            onValidate()
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
            pinError = wrongPinText
            failedTries++
            pinShapes.clear()
            pin = ""
        }
    }
}


/**
 * Dialog for setting up a new PIN (enter + confirm).
 */
@Composable
public fun PinSetup(
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirmStep by remember { mutableStateOf(false) }

    var showWarningDialog by remember { mutableStateOf(false) }
    val doNotRemindMeWarningDialog by UiSettingsStore.doNotRemindMeAgainPinLockWarning.asState()

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedTries by remember { mutableIntStateOf(0) }
    val pinMismatch = stringResource(R.string.pin_mismatch)

    val pinShapes = remember(isConfirmStep) { mutableStateListOf<RoundedPolygon>() }
    val currentPin = if (isConfirmStep) confirmPin else firstPin

    PinPrompt(
        title = stringResource(R.string.set_pin),
        subtitle = if (isConfirmStep) stringResource(R.string.confirm_pin) else stringResource(R.string.enter_pin),
        pinValue = currentPin,
        pinShapes = pinShapes,
        errorMessage = errorMessage,
        failedTries = failedTries,
        onPinChanged = { newValue ->
            errorMessage = null
            if (pinShapes.size < newValue.length) {
                repeat(newValue.length - pinShapes.size) {
                    pinShapes.add(pinMaterialShapes.random())
                }
            } else {
                repeat(pinShapes.size - newValue.length) {
                    pinShapes.removeAt(pinShapes.lastIndex)
                }
            }
            if (isConfirmStep) {
                confirmPin = newValue
            } else {
                firstPin = newValue
            }
        },
        onDismiss = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            if (isConfirmStep) {
                isConfirmStep = false
                confirmPin = ""
                errorMessage = null
            } else {
                onDismiss()
            }
        }
    ) {
        if (!isConfirmStep) {
            isConfirmStep = true
            confirmPin = ""
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        } else {
            when {
                // Error
                firstPin != confirmPin -> {
                    errorMessage = pinMismatch
                    confirmPin = ""
                    pinShapes.clear()
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
        }
    }
    
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    if (showWarningDialog) {
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
            onPinSet(firstPin)
        }
        
    }
}

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
private fun PinPrompt(
    title: String,
    subtitle: String,
    pinValue: String,
    pinShapes: List<RoundedPolygon>,
    errorMessage: String? = null,
    failedTries: Int,
    minDigits: Int = 1,
    maxDigits: Int = Int.MAX_VALUE,
    onPinChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onPrimaryAction: () -> Unit
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

                PinIndicator(pinShapes)

                AnimatedVisibility(errorMessage != null) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            NumericPinPad(
                modifier = Modifier.fillMaxWidth(),
                onDigit = { digit ->
                    if (pinValue.length < maxDigits) {
                        onPinChanged(pinValue + digit)
                    }
                },
                validateEnabled = pinValue.length >= minDigits,
                onValidate = onPrimaryAction,
                backSpaceOrClose = pinValue.isNotEmpty(),
                onClear = {
                    if (pinValue.isEmpty()) onDismiss()
                    else onPinChanged("")
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PinIndicator(
    shapes: List<RoundedPolygon>
) {
    val scope = rememberCoroutineScope()
    val lazyState = rememberLazyListState()

    LaunchedEffect(shapes.size) {
        if (shapes.isNotEmpty()){
            scope.launch { lazyState.scrollToItem(shapes.lastIndex) }
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        state = lazyState
    ) {
        items(shapes) { shape ->
            var scaleTarget by remember { mutableFloatStateOf(0f) }

            // Trigger visibility only once when shape is added
            // I find this genius
            LaunchedEffect(shape) {
                scaleTarget = 1f
            }

            val scale by animateFloatAsState(
                targetValue = scaleTarget,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )

            Box(
                modifier = Modifier
                    .size(25.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape.toShape()
                    )
            )
        }
    }
}


@Composable
private fun NumericPinPad(
    modifier: Modifier,
    validateEnabled: Boolean,
    backSpaceOrClose: Boolean,
    onDigit: (String) -> Unit,
    onValidate: () -> Unit,
    onClear: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )

    val spacing = 20.dp


    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                row.forEach { digit ->
                    KeypadButton(
                        text = digit,
                        modifier = Modifier.weight(1f),
                        onClick = onDigit
                    )
                }
            }
        }



        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            AnimatedContent(
                targetState = backSpaceOrClose,
                modifier = Modifier.weight(1f)
            ) {

                KeypadButton(
                    icon = if (it) R.drawable.backspace else R.drawable.close,
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onClear
                )
            }

            KeypadButton(
                text = "0",
                modifier = Modifier.weight(1f),
                onClick = onDigit
            )

            KeypadButton(
                icon = R.drawable.check,
                tint = Color.Green,
                modifier = Modifier.weight(1f),
                onClick = onValidate,
                enabled = validateEnabled
            )
        }
    }
}

@Composable
private fun KeypadButton(
    modifier: Modifier = Modifier,
    icon: Int,
    tint: Color,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {

    Box(
        modifier = modifier.keyPadModifier(enabled, onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painterResource(id = icon),
            contentDescription = null,
            tint = tint
        )
    }
}

@Composable
private fun KeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
) {

    Box(
        modifier = modifier.keyPadModifier { onClick(text) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
    }
}


@Composable
private fun Modifier.keyPadModifier(
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier {

    return this
        .aspectRatio(1f)
        .then(
            onClick?.let { click ->
                Modifier.shapedClickable(
                    enabled = enabled,
                    onClick = click
                )
            } ?: Modifier
        )
        .background(MaterialTheme.colorScheme.surface.semiTransparentIfDisabled(enabled))
        .padding(15.dp)
}


@Composable
public fun PlayWarningSounds(
    failedTries: Int,
    superWarningMode: Boolean,
    superWarningModeSound: Int,
    alarmSoundEnabled: Boolean,
    metalPipesSoundEnabled: Boolean
) {
    val ctx = LocalContext.current

    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    var alarmLoaded by remember { mutableStateOf(false) }
    var metalLoaded by remember { mutableStateOf(false) }

    val alarmSoundId = remember {
        soundPool.load(ctx, R.raw.warning, 1)
    }

    val metalSoundId = remember {
        soundPool.load(ctx, R.raw.metal_pipe, 1)
    }

    DisposableEffect(Unit) {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                if (sampleId == alarmSoundId) alarmLoaded = true
                if (sampleId == metalSoundId) metalLoaded = true
            }
        }

        onDispose { soundPool.release() }
    }

    LaunchedEffect(failedTries, superWarningMode) {
        if (failedTries > 0 && superWarningMode && superWarningModeSound > 0) {

            val audioManager =
                ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            val maxVolume =
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                superWarningModeSound.coerceIn(0, maxVolume),
                0
            )

            if (alarmSoundEnabled && alarmLoaded) {
                soundPool.play(alarmSoundId, 1f, 1f, 1, -1, 1f)
            }

            if (metalPipesSoundEnabled && metalLoaded) {
                soundPool.play(metalSoundId, 1f, 1f, 1, 0, 1f)
            }
        }
    }
}
