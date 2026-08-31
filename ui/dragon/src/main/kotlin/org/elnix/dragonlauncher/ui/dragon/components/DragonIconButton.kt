package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DragonIconButtonImpl(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
    colors: IconButtonColors,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        shapes = IconButtonDefaults.shapes(),
        content = content
    )
}

@Composable
fun DragonIconButton(
    icon: Int,
    contentDescription: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isCancel: Boolean = false,
    interactionSource: MutableInteractionSource = rememberInteractionSource(),
    onClick: () -> Unit
) {
    val colors = if (isCancel) AppObjectsColors.cancelIconButtonColors() else AppObjectsColors.iconButtonColors()

    DragonTooltip(contentDescription) {
        DragonIconButtonImpl(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            interactionSource = interactionSource,
            colors = colors
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = stringResource(contentDescription)
            )
        }
    }
}

// @Composable
// fun DragonIconButton(
//    icon: Int,
//    contentDescription: String,
//    modifier: Modifier = Modifier,
//    enabled: Boolean = true,
//    isCancel: Boolean = false,
//    interactionSource: MutableInteractionSource = rememberInteractionSource(),
//    onClick: () -> Unit
// ) {
//    val colors = if (isCancel) AppObjectsColors.cancelIconButtonColors() else AppObjectsColors.iconButtonColors()
//
//    DragonTooltip(contentDescription) {
//        DragonIconButtonImpl(
//            onClick = onClick,
//            modifier = modifier,
//            enabled = enabled,
//            interactionSource = interactionSource,
//            colors = colors
//        ) {
//            Icon(
//                painter = painterResource(icon),
//                contentDescription = contentDescription
//            )
//        }
//    }
// }
