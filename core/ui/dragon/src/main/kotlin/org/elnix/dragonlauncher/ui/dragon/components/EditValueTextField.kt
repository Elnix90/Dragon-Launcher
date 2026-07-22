package org.elnix.dragonlauncher.ui.dragon.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.animation.barsContentTransform

@Suppress("DEPRECATION")
@Composable
public fun EditValueTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    resetEnabled: Boolean,
    textColor: Color? = null,
    isError: Boolean = false,
    backgroundColor: Color,
    onReset: () -> Unit,
    onFocusChange: ((Boolean) -> Unit)? = null,
    onDone: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    var isEditing by remember { mutableStateOf(false) }

    // If the user presses back when editing, the value is commited (I use that because I do back to quit the slider label thing)
    BackHandler(isEditing) {
        onDone()
    }

    // Observe focus via InteractionSource
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

    LaunchedEffect(isEditing) {
        onFocusChange?.invoke(isEditing)
    }

    TextField(
        enabled = enabled,
        interactionSource = interactionSource,
        value = value,
        onValueChange = { raw ->
            onValueChange(raw)
        },
        textStyle = TextStyle(
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        ),
        isError = isError,
        trailingIcon = {
            AnimatedContent(
                targetState = isEditing,
                transitionSpec = { barsContentTransform },
                label = "icon_button_transition"
            ) { editing ->
                when {
                    editing -> {
                        DragonIconButton(
                            onClick = onDone,
                            colors = IconButtonDefaults.iconButtonColors(containerColor = backgroundColor),
                            icon = R.drawable.check,
                            contentDescription = "Validate"
                        )
                    }

                    else -> {
                        ResetIcon(
                            onReset = onReset,
                            enabled = enabled && resetEnabled,
                        )
                    }
                }
            }
        },
        colors = AppObjectsColors.outlinedTextFieldColors(
            removeBorder = true,
            backgroundColor = backgroundColor,
            onBackgroundColor = textColor
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.DecimalSigned,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),
        shape = CircleShape,
        modifier = modifier
            .width(120.dp)
            .height(50.dp)
    )
}