package org.elnix.dragonlauncher.ui.dragon.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.UiConstants

@Composable
fun EditValueTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    textColor: Color? = null,
    backgroundColor: Color,
    onFocusChange: ((Boolean) -> Unit)? = null,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
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


    val shapeRound = remember {
        Animatable(
            initialValue = UiConstants.DRAGON_SHAPE_CORNER_PERCENT.toFloat(),
        )
    }

    // Animate to dragon shape on focus change
    LaunchedEffect(isEditing) {
        onFocusChange?.invoke(isEditing)
        scope.launch {

            shapeRound.animateTo(
                if (isEditing) {
                    UiConstants.PRESSED_DRAGON_SHAPE_CORNER_PERCENT.toFloat()
                } else {
                    UiConstants.DRAGON_SHAPE_CORNER_PERCENT.toFloat()
                }
            )
        }
    }


    val shape = RoundedCornerShape(shapeRound.value.dp)

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
        colors = AppObjectsColors.outlinedTextFieldColors(
            removeBorder = true,
            backgroundColor = backgroundColor,
            onBackgroundColor = textColor

        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),
        shape = shape,
        modifier = Modifier
            .width(80.dp)
            .height(50.dp)
    )
}