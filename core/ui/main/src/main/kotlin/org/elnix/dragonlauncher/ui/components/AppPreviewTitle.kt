package org.elnix.dragonlauncher.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.ui.actions.PointIcon
import org.elnix.dragonlauncher.ui.actions.actionLabel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun AppPreviewTitle(
    point: Point?,
    topPadding: Dp = 60.dp,
    labelSize: Int,
    showLabel: Boolean,
    showIcon: Boolean
) {
    if (point == null) return

    val extraColors = LocalExtraColors.current

    val label = point.customName ?: actionLabel(point.action)



    val alpha = remember { Animatable(initialValue = 0f) }
    val offsetY = remember { Animatable(initialValue = -20f) }

    LaunchedEffect(point) {
        alpha.snapTo(0f)
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(150)
        )
    }

    LaunchedEffect(point) {
        offsetY.snapTo(-20f)
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(150)
        )
    }

    val action = point.action
    if (showIcon || showLabel) {
        Box(
            Modifier
                .fillMaxWidth()
                .offset(y = offsetY.value.dp)
                .padding(top = topPadding)
                .alpha(alpha.value),
            contentAlignment = Alignment.TopCenter
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val colorAction =
                    action.actionColor(extraColors, point.customActionColor?.let { Color(it) })


                if (showIcon) {
                    PointIcon(point)
                }

                if (showLabel) {
                    Text(
                        text = label,
                        style = TextStyle(
                            color = colorAction,
                            fontSize = labelSize.sp,
                            fontWeight = FontWeight.Bold,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.48f),
                                offset = Offset(0f, 1f),
                                blurRadius = 5f
                            )
                        )
                    )
                }
            }
        }
    }
}
