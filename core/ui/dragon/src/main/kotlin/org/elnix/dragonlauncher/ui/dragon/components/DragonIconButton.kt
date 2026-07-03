package org.elnix.dragonlauncher.ui.dragon.components

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


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DragonIconButtonImpl(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        shapes = IconButtonDefaults.shapes(),
        content = content
    )
}

@Composable
fun DragonIconButton(
    modifier: Modifier = Modifier,
    enabled: () -> Boolean = { true },
    colors: IconButtonColors = AppObjectsColors.iconButtonColors(),
    icon: Int,
    contentDescription: Int,
    onClick: () -> Unit
) {

    DragonTooltip(contentDescription) {
        DragonIconButtonImpl(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled(),
            colors = colors
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = stringResource(contentDescription)
            )
        }
    }
}

@Composable
fun DragonIconButton(
    icon: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: () -> Boolean = { true },
    colors: IconButtonColors = AppObjectsColors.iconButtonColors(),
    onClick: () -> Unit
) {

    DragonTooltip(contentDescription) {
        DragonIconButtonImpl(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled(),
            colors = colors
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = contentDescription
            )
        }
    }
}
