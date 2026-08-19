package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.elnix90.core.objects.StringSettingObject
import io.github.elnix90.runtime.asMutableState
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.animation.Icon
import org.elnix.dragonlauncher.ui.base.animation.rememberAnimatedIcon
import org.elnix.dragonlauncher.ui.composition.LocalSettingsPlacementChecker

@Composable
fun Setting(
    setting: StringSettingObject,
    enabled: Boolean = true,
    singleChar: Boolean = true,
    singleLine: Boolean = true
) {
    // Craches if this setting isn't placed inside a DragonSettingsGroup
    LocalSettingsPlacementChecker.current

    var state by setting.asMutableState()
    var tempState by rememberSaveable { mutableStateOf(state) }

    var isEditing by remember { mutableStateOf(false) }

    // Sync the text with external value changes (slider drag, programmatic updates).
    // The state must NOT be re-created on valueText change: the focus-interaction
    // collector below captures `onDone` once, and re-creating the state would make it
    // read an orphaned/stale value after the first commit.
    LaunchedEffect(state) {
        if (!isEditing) tempState = state
    }

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val animatedIcon = rememberAnimatedIcon()

    fun onDone() {
        animatedIcon.setSuccess()
        focusManager.clearFocus(true)
        state = tempState
    }

    BackHandler(isEditing, onBack = ::onDone)

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is FocusInteraction.Focus -> {
                    isEditing = true
                }

                is FocusInteraction.Unfocus -> {
                    onDone()
                    isEditing = false
                }
            }
        }
    }

    TextField(
        value = tempState,
        onValueChange = {
            if (singleChar && it.length > 1) {
                animatedIcon.setError()
                return@TextField
            }
            tempState = it
        },
        label = { Text((stringResource(setting.title!!))) },
        placeholder = { Text((stringResource(setting.description!!))) },
        colors = AppObjectsColors.outlinedTextFieldColors(
            removeBorder = true
        ),
        shape = CircleShape,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(1f),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onDone()
            }
        ),
        trailingIcon = {
            val showReset = !isEditing

            AnimatedContent(showReset) { showReset ->
                animatedIcon.Icon(
                    defaultIcon = if (showReset) R.drawable.reset else R.drawable.check,
                    successIcon = if (showReset) R.drawable.check else R.drawable.save,
                    enabled = enabled && if (showReset) tempState != setting.default else tempState != state
                ) {
                    if (showReset) {
                        tempState = setting.default
                        state = setting.default

                        animatedIcon.setSuccess()
                        focusManager.clearFocus()
                    } else {
                        onDone()
                    }
                }
            }
        }
    )
}
