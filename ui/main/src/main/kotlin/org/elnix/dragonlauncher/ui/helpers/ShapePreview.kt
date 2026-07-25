package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.ui.base.modifiers.conditional

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShapePreview(
    iconShape: IconShape,
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val bgColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondary
        else Color.Transparent
    )

    val borderColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondary
        else MaterialTheme.colorScheme.primaryContainer
    )

    Box(
        modifier = modifier
            .aspectRatio(1f, true)
            .size(size)
            .clip(MaterialTheme.shapes.extraLarge)
            .conditional(onClick) {
                combinedClickable(onLongClick = onLongClick, onClick = it)
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(size / 5)
        ) {
            when (iconShape) {

                is IconShape.Random -> {
                    Icon(
                        painter = painterResource(R.drawable.shuffle),
                        contentDescription = stringResource(R.string.random_shape),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.large)
                            .background(bgColor.alphaMultiplier(0.5f))
                            .border(1.dp, borderColor, MaterialTheme.shapes.large)
                    )
                }

                else -> {
                    val shape = iconShape.resolveShape()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shape)
                            .background(bgColor.alphaMultiplier(0.5f))
                            .border(1.dp, borderColor, shape)
                    )
                }
            }
        }
    }
}
