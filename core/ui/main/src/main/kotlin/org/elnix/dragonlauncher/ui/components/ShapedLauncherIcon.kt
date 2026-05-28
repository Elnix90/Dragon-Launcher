package org.elnix.dragonlauncher.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.icu.text.NumberFormat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.badges.Badge
import org.elnix.dragonlauncher.badges.BadgeIcon
import org.elnix.dragonlauncher.base.icons.ClockLayer
import org.elnix.dragonlauncher.base.icons.ClockSublayer
import org.elnix.dragonlauncher.base.icons.ClockSublayerRole
import org.elnix.dragonlauncher.base.icons.DynamicLauncherIcon
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.LauncherIconRenderSettings
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TextLayer
import org.elnix.dragonlauncher.base.icons.TintedClockLayer
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.icons.VectorLayer
import org.elnix.dragonlauncher.common.messyfolder.resolveShape
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.ktx.drawWithColorFilter
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.ui.base.compositionslocals.LocalTime
import org.elnix.dragonlauncher.ui.composition.LocalIconShape
import palettes.TonalPalette
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt
import android.graphics.Shader as PlatformShader

@Composable
fun ShapedLauncherIcon(
    modifier: Modifier = Modifier,
    maxIconSize: Dp,
    icon: () -> LauncherIcon? = { null },
    badge: () -> Badge? = { null },
    shape: IconShape = LocalIconShape.current
) {

    val icon = icon()
    val shape = shape.resolveShape()

    var currentIcon by remember(icon) {
        mutableStateOf(
            when (icon) {
                is DynamicLauncherIcon -> null
                is StaticLauncherIcon -> icon
                else -> null
            }
        )
    }


    val renderSettings = LauncherIconRenderSettings(
        size = maxIconSize.px.toInt(),
        fgThemeColor = MaterialTheme.colorScheme.onPrimaryContainer.toArgb(),
        bgThemeColor = MaterialTheme.colorScheme.primaryContainer.toArgb(),
        fgTone = 90,
        bgTone = 30,
//        fgTone = if (LocalDarkTheme.current) 90 else 10,TODO
//        bgTone = if (LocalDarkTheme.current) 30 else 90,
    )

    var currentBitmap by remember {
        mutableStateOf(currentIcon?.getCachedBitmap(renderSettings))
    }

    LaunchedEffect(currentIcon, renderSettings) {
        currentBitmap = currentIcon?.render(renderSettings)
    }

    if (icon is DynamicLauncherIcon) {
        val date = Instant.ofEpochMilli(LocalTime.current).atZone(ZoneId.systemDefault())
        LaunchedEffect(date.dayOfYear, icon) {
            currentIcon = icon.getIcon(date.toEpochSecond() * 1000L)
        }
    }

    Box(
        modifier = modifier
            .sizeIn(maxWidth = maxIconSize, maxHeight = maxIconSize)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val bmp = currentBitmap
            val ic = currentIcon
            if (bmp != null && ic != null) {
                Canvas(
                    modifier = Modifier
                        .requiredSize(maxIconSize)
//                        .scale(maxIconSize / defaultIconSize, TransformOrigin.Center)
                ) {
                    val brush = BitmapShaderBrush(bmp)
                    if (ic.backgroundLayer is TransparentLayer) {
                        drawRect(brush)
                    } else {
                        val outline =
                            shape.createOutline(
                                this.size,
                                layoutDirection,
                                Density(density, fontScale)
                            )
                        drawOutline(outline, brush)
                    }
                }
                // Background layer is always static layer, color layer, or transparent layer
                val fg = ic.foregroundLayer
                when (fg) {
                    is ClockLayer -> {
                        ClockLayer(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(shape),
                            sublayers = fg.sublayers,
                            defaultMinute = fg.defaultMinute,
                            defaultHour = fg.defaultHour,
                            defaultSecond = fg.defaultSecond,
                            scale = fg.scale,
                            tintColor = null,
                        )
                    }

                    is TintedClockLayer -> {
                        ClockLayer(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(shape),
                            sublayers = fg.sublayers,
                            defaultMinute = fg.defaultMinute,
                            defaultHour = fg.defaultHour,
                            defaultSecond = fg.defaultSecond,
                            scale = fg.scale,
                            tintColor = if (fg.color == 0) {
                                Color(renderSettings.fgThemeColor)
                            } else {
                                Color(getTone(fg.color, renderSettings.fgTone))
                            },
                        )
                    }

                    is TextLayer -> {
                        Text(
                            text = fg.text,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 20.sp * (maxIconSize / 48.dp)
                            ),
                            color = if (fg.color == 0) {
                                Color(renderSettings.fgThemeColor)
                            } else {
                                Color(getTone(fg.color, renderSettings.fgTone))
                            },
                        )
                    }

                    is VectorLayer -> {
                        Icon(
                            painter = painterResource(fg.icon), contentDescription = null,
                            tint = if (fg.color == 0) {
                                Color(renderSettings.fgThemeColor)
                            } else {
                                Color(getTone(fg.color, renderSettings.fgTone))
                            },
                            modifier = Modifier.size(maxIconSize / 2f),
                        )
                    }
                    else -> {}
                }
            } else {
                val color = MaterialTheme.colorScheme.secondaryContainer
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    val outline =
                        shape.createOutline(this.size, layoutDirection, Density(density, fontScale))
                    drawOutline(outline, color)
                }
            }
        }
        val badge = badge()
        if (badge != null) {
            Surface(
                tonalElevation = 1.dp,
                modifier = Modifier
                    .size(maxIconSize * 0.33f)
                    .align(Alignment.BottomEnd),
                color = MaterialTheme.colorScheme.tertiary,
                shape = CircleShape
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {

                    badge.progress?.let {
                        val progress by animateFloatAsState(it)
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxSize(0.8f),
                            progress = { progress },
                            strokeWidth = maxIconSize / 48,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                    val badgeIcon = badge.icon

                    val number = badge.number
                    if (badgeIcon is BadgeIcon.Vector) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(maxIconSize / 24),
                            painter = painterResource(badgeIcon.iconRes),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary,
                        )
                    } else if (badgeIcon is BadgeIcon.Drawable) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(maxIconSize / 48)
                        ) {
                            badgeIcon.drawable.setBounds(
                                0,
                                0,
                                this.size.width.roundToInt(),
                                this.size.height.roundToInt()
                            )
                            drawIntoCanvas {
                                badgeIcon.drawable.draw(it.nativeCanvas)
                            }
                        }
                    } else if (number != null && number > 0 && number < 100) {
                        Text(
                            NumberFormat.getInstance(Locale.current.platformLocale).format(number),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = with(LocalDensity.current) {
                                    maxIconSize.toSp() * 0.2f
                                }
                            ),
                        )
                    }
                }
            }
        }
    }
}

private fun getTone(argb: Int, tone: Int): Int {
    return TonalPalette
        .fromInt(argb)
        .tone(tone)
}

@Composable
private fun ClockLayer(
    sublayers: List<ClockSublayer>,
    defaultMinute: Int,
    defaultHour: Int,
    defaultSecond: Int,
    scale: Float,
    tintColor: Color?,
    modifier: Modifier = Modifier,
) {
    val time = Instant.ofEpochMilli(LocalTime.current).atZone(ZoneId.systemDefault())

    val second = time.second
    val minute = time.minute
    val hour = time.hour

    Canvas(modifier = modifier) {
        val colorFilter = tintColor?.let {
            PorterDuffColorFilter(tintColor.toArgb(), PorterDuff.Mode.SRC_IN)
        }
        withTransform({
            this.scale(scale)
        }) {
            for (sublayer in sublayers) {
                when (sublayer.role) {
                    ClockSublayerRole.Hour -> {
                        sublayer.drawable.level = (((hour - defaultHour + 12) % 12) * 60
                                + ((minute) % 60))
                    }

                    ClockSublayerRole.Minute -> sublayer.drawable.level =
                        ((minute - defaultMinute + 60) % 60)

                    ClockSublayerRole.Second -> sublayer.drawable.level =
                        (((second - defaultSecond + 60) % 60) * 10)

                    else -> {}
                }
                drawIntoCanvas {
                    sublayer.drawable.bounds = this.size.toRect().toAndroidRect()
                    sublayer.drawable.drawWithColorFilter(it.nativeCanvas, colorFilter)
                }
            }
        }
    }
}

class BitmapShaderBrush(
    val bitmap: Bitmap,
) : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        return BitmapShader(bitmap, PlatformShader.TileMode.CLAMP, PlatformShader.TileMode.CLAMP)
    }
}