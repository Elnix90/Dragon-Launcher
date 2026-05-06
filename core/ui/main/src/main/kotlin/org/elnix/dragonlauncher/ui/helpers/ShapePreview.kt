package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.ColorUtils.alphaMultiplier
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.messyfolder.resolveShape
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable

@Composable
fun ShapePreview(
    iconShape: IconShape,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {

    val bgColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface
    )

    Box(
        modifier = Modifier
            .padding(5.dp)
            .then(modifier)
            .aspectRatio(1f, true)
            .clip(DragonShape)
            .conditional(onClick) {
                shapedClickable(onClick = it)
            },
        contentAlignment = Alignment.Center
    ) {
        if (iconShape !is IconShape.Random) {
            val shape = iconShape.resolveShape()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
                    .clip(shape)
                    .background(bgColor.alphaMultiplier(0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.secondary, shape)
            )
        } else {

            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = stringResource(R.string.random_shape),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
                    .clip(DragonShape)
                    .background(bgColor.alphaMultiplier(0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.secondary, DragonShape)
            )
        }
    }
}
