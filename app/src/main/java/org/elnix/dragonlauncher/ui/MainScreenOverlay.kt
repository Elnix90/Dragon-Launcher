@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.SwipePointSerializable
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.theme.AmoledDefault
import org.elnix.dragonlauncher.utils.actions.actionColor
import org.elnix.dragonlauncher.utils.actions.actionIcon
import org.elnix.dragonlauncher.utils.actions.actionIconBitmap
import org.elnix.dragonlauncher.utils.actions.actionLabel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun MainScreenOverlay(
    icons: Map<String, ImageBitmap>,
    start: Offset?,
    current: Offset?,
    isDragging: Boolean,
    surface: IntSize,
    points: List<SwipePointSerializable>,
    onLaunch: (SwipeActionSerializable?) -> Unit
) {
    val ctx = LocalContext.current

    val rgbLine by UiSettingsStore.getRGBLine(ctx)
        .collectAsState(initial = true)
    val debugInfos by DebugSettingsStore.getDebugInfos(ctx)
        .collectAsState(initial = false)
    val angleLineColor by ColorSettingsStore.getAngleLineColor(ctx)
        .collectAsState(initial = AmoledDefault.AngleLineColor)
    val circleColor by ColorSettingsStore.getCircleColor(ctx)
        .collectAsState(initial = AmoledDefault.CircleColor)
    val showLaunchingAppLabel by UiSettingsStore.getShowLaunchingAppLabel(ctx)
        .collectAsState(initial = true)
    val showLaunchingAppIcon by UiSettingsStore.getShowLaunchingAppIcon(ctx)
        .collectAsState(initial = true)

    val showAppLaunchPreview by UiSettingsStore.getShowAppLaunchPreview(ctx)
        .collectAsState(initial = true)
    val showAppCirclePreview by UiSettingsStore.getShowCirclePreview(ctx)
        .collectAsState(initial = true)
    val showAppLinePreview by UiSettingsStore.getShowLinePreview(ctx)
        .collectAsState(initial = true)

    val backgroundColor = MaterialTheme.colorScheme.background

    var lastAngle by remember { mutableStateOf<Double?>(null) }
    var cumulativeAngle by remember { mutableDoubleStateOf(0.0) }   // continuous rotation without jumps


    val dx: Float
    val dy: Float
    val dist: Float
    val angleRad: Double
    val angleDeg: Double
    val angle0to360: Double

    val lineColor: Color

    if (start != null && current != null) {
        dx = current.x - start.x
        dy = current.y - start.y
        dist = hypot(dx, dy)

        // angle relative to UP = 0°
        angleRad = atan2(dx.toDouble(), -dy.toDouble())
        angleDeg = Math.toDegrees(angleRad)
        angle0to360 = if (angleDeg < 0) angleDeg + 360 else angleDeg

        // --- smooth 360° tracking ---
        lastAngle?.let { prev ->
            val diff = angle0to360 - prev

            val adjustedDiff = when {
                diff > 180  -> diff - 360   // jumped CW past 360→0
                diff < -180 -> diff + 360   // jumped CCW past 0→360
                else -> diff                // normal small movement
            }

            cumulativeAngle += adjustedDiff
        }
        @Suppress("AssignedValueIsNeverRead")
        lastAngle = angle0to360


        lineColor = if (rgbLine) Color.hsv(angle0to360.toFloat(),1f,1f)
                    else angleLineColor ?: AmoledDefault.AngleLineColor

    } else {
        dx = 0f; dy = 0f
        dist = 0f
        angleDeg = 0.0
        angle0to360 = 0.0
        cumulativeAngle = 0.0
        lineColor = Color.Transparent
    }

    val sweepAngle = (cumulativeAngle % 360).toFloat()


    // Launch app logic

    // -- For displaying the banner --
    var hoveredAction by remember { mutableStateOf<SwipePointSerializable?>(null) }
    var bannerVisible by remember { mutableStateOf(false) }

    // Distance thresholds for 3 circles
    val circleR1 = 400f
    val circleR2 = 700f

    // Safe zone = dragging returns close to origin → cancel
    val cancelZone = 150f

    // The chosen swipe action
    var currentAction: SwipePointSerializable? by remember { mutableStateOf(null) }

    val targetCircle =
        when {
            dist < circleR1 -> 0
            dist < circleR2 -> 1
            else -> 2
        }

    if (start != null && current != null && isDragging) {

        val closestPoint =
            points.filter { it.circleNumber == targetCircle }
                .minByOrNull {
                    val d = kotlin.math.abs(it.angleDeg - angle0to360)
                    minOf(d, 360 - d)
                }

        currentAction = if (dist > cancelZone) closestPoint else null

        hoveredAction = currentAction
        bannerVisible = currentAction != null
    } else if (!isDragging) {
        bannerVisible = false
    }

    val alpha by animateFloatAsState(
        targetValue = if (bannerVisible) 1f else 0f,
        animationSpec = tween(150)
    )
    val offsetY by animateDpAsState(
        targetValue = if (bannerVisible) 0.dp else (-20).dp,
        animationSpec = tween(150)
    )

    LaunchedEffect(isDragging) {
        if (!isDragging) {
            if (currentAction != null) {
                onLaunch(currentAction?.action)
            }
            hoveredAction = null
            currentAction = null
            bannerVisible = false
        }
    }

    Box(Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (debugInfos) {
                Text(
                    text = "start = ${start?.let { "%.1f, %.1f".format(it.x, it.y) } ?: "—"}",
                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium
                )
                Text(
                    text = "current = ${current?.let { "%.1f, %.1f".format(it.x, it.y) } ?: "—"}",
                    color = Color.White, fontSize = 12.sp)
                Text(
                    text = "dx = %.1f   dy = %.1f".format(dx, dy),
                    color = Color.White, fontSize = 12.sp
                )
                Text(
                    text = "dist = %.1f".format(dist),
                    color = Color.White, fontSize = 12.sp
                )
                Text(
                    text = "angle raw = %.1f°".format(angleDeg),
                    color = Color.White, fontSize = 12.sp
                )
                Text(
                    text = "angle 0–360 = %.1f°".format(angle0to360),
                    color = Color.White, fontSize = 12.sp
                )
                Text(
                    text = "drag = $isDragging, size = ${surface.width}×${surface.height}",
                    color = Color.White, fontSize = 12.sp
                )
                Text(
                    text = "target circle = $targetCircle",
                    color = Color.White, fontSize = 12.sp
                )
                Text(
                    text = "current action = $currentAction",
                    color = Color.White, fontSize = 12.sp
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (start != null && current != null) {
                if (showAppLinePreview) {
                    val circleRadius = 48f
                    drawCircle(
                        color = lineColor,
                        radius = circleRadius,
                        center = start,
                        style = Stroke(width = 3f)
                    )

                    drawLine(
                        color = lineColor,
                        start = start,
                        end = current,
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )

                    drawCircle(
                        color = backgroundColor,
                        radius = circleRadius - 2,
                        center = start
                    )

                    drawCircle(
                        color = lineColor,
                        radius = 8f,
                        center = current,
                        style = Fill
                    )

                    val arcRadius = 72f
                    val rect = Rect(
                        start.x - arcRadius,
                        start.y - arcRadius,
                        start.x + arcRadius,
                        start.y + arcRadius
                    )

                    drawArc(
                        color = lineColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = Size(rect.width, rect.height),
                        style = Stroke(width = 3f)
                    )
                }

                if (showAppCirclePreview || showAppLinePreview || showAppLaunchPreview) {
                    hoveredAction?.let { point ->
                        val action = point.action!!
                        if (showAppCirclePreview) {
                            drawCircle(
                                color = circleColor ?: AmoledDefault.CircleColor,
                                radius = 200f + (targetCircle * 140f),
                                center = start,
                                style = Stroke(4f)
                            )
                        }


                        // Draw actual selected point
                        // find the matching SwipePointSerializable
                        val point = points.firstOrNull { it == point }
                        if (point != null) {

                            // same circle radii as SettingsScreen
                            val radius = when (point.circleNumber) {
                                0 -> 200f
                                1 -> 340f
                                else -> 480f
                            }

                            // compute point position relative to origin
                            val px = start.x +
                                    radius * sin(Math.toRadians(point.angleDeg)).toFloat()
                            val py = start.y -
                                    radius * cos(Math.toRadians(point.angleDeg)).toFloat()


                            if (showAppCirclePreview) {
                                drawCircle(
                                    color = circleColor ?: AmoledDefault.CircleColor,
                                    radius = 44f,
                                    center = Offset(px, py)
                                )

                                drawCircle(
                                    color = backgroundColor,
                                    radius = 40f,
                                    center = Offset(px, py)
                                )
                            }
                            if (showAppLaunchPreview) {
                                drawImage(
                                    image = actionIconBitmap(
                                        action = action,
                                        context = ctx,
                                        tintColor = actionColor(action)
                                    ),
                                    dstOffset = IntOffset(px.toInt() - 28, py.toInt() - 28),
                                    dstSize = IntSize(56, 56)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (hoveredAction != null && (showLaunchingAppLabel || showLaunchingAppIcon)) {
        val currentAction = hoveredAction!!.action!!
            Box(
            Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .padding(top = 20.dp)
                .alpha(alpha),
            contentAlignment = Alignment.TopCenter
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showLaunchingAppIcon) {
                    Icon(
                        painter = actionIcon(currentAction, icons),
                        contentDescription = actionLabel(currentAction),
                        tint = actionTint(currentAction),
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (showLaunchingAppLabel) {
                    Text(
                        text = actionLabel(currentAction),
                        color = actionColor(currentAction),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun actionTint(action: SwipeActionSerializable): Color =
    when (action) {
        is SwipeActionSerializable.LaunchApp, SwipeActionSerializable.OpenDragonLauncherSettings  -> Color.Unspecified
        else -> actionColor(action)
    }
