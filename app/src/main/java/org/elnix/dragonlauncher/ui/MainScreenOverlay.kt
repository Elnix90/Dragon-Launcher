@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.data.CircleNest
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.SwipePointSerializable
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.components.AppPreviewTitle
import org.elnix.dragonlauncher.ui.helpers.actionsInCircle
import org.elnix.dragonlauncher.ui.theme.AmoledDefault
import org.elnix.dragonlauncher.ui.theme.ExtraColors
import org.elnix.dragonlauncher.ui.theme.LocalExtraColors
import org.elnix.dragonlauncher.utils.actions.actionColor
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
    nestId: Int,
    isDragging: Boolean,
    surface: IntSize,
    points: List<SwipePointSerializable>,
    nests: List<CircleNest>,
    onLaunch: (SwipePointSerializable?) -> Unit
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
    val showAppAnglePreview by UiSettingsStore.getShowAnglePreview(ctx)
        .collectAsState(initial = true)
    val showAppPreviewIconCenterStartPosition by UiSettingsStore.getShowAppPreviewIconCenterStartPosition(ctx)
        .collectAsState(initial = false)
    val linePreviewSnapToAction by UiSettingsStore.getLinePreviewSnapToAction(ctx)
        .collectAsState(initial = false)
    val showAllActionsOnCurrentCircle by UiSettingsStore.getShowAllActionsOnCurrentCircle(ctx)
        .collectAsState(initial = false)
    val showActionIconBorder by UiSettingsStore.getShowActionIconBorder(ctx)
        .collectAsState(initial = false)

//    val backgroundColor = MaterialTheme.colorScheme.background
    val extraColors = LocalExtraColors.current


    var lastAngle by remember { mutableStateOf<Double?>(null) }
    var cumulativeAngle by remember { mutableDoubleStateOf(0.0) }   // continuous rotation without jumps


    val minAngleFromAPointToActivateIt by UiSettingsStore.getMinAngleFromAPointToActivateIt(ctx)
        .collectAsState(initial = 0)




    val dragRadii = nests.find { it.id == nestId }?.dragDistances ?: CircleNest().dragDistances

    val dx: Float
    val dy: Float
    val dist: Float
    val angleRad: Double
    val angleDeg: Double
    val angle0to360: Double

    val lineColor: Color
    val circleRadius = 48f

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

    var exposedClosest by remember { mutableStateOf<SwipePointSerializable?>(null) }
    var exposedAsbAngle by remember { mutableStateOf<Double?>(null) }

    // Launch app logic

    // -- For displaying the banner --
    var hoveredAction by remember { mutableStateOf<SwipePointSerializable?>(null) }
    var bannerVisible by remember { mutableStateOf(false) }


    // The chosen swipe action
    var currentAction: SwipePointSerializable? by remember { mutableStateOf(null) }


    // The circle that corresponds to the distance of which the user drags
    val targetCircle = dragRadii.entries
        .firstOrNull { (_, distance) -> dist <= distance }
        ?.key
        ?: dragRadii.keys.maxOrNull() ?: -1



    if (start != null && current != null && isDragging) {

        val closestPoint =
            points.filter { it.nestId == nestId && it.circleNumber == targetCircle }
                .minByOrNull {
                    val d = kotlin.math.abs(it.angleDeg - angle0to360)
                    minOf(d, 360 - d)
                }

        exposedClosest = closestPoint

        val selectedPoint = closestPoint?.let { p ->
            val d = kotlin.math.abs(p.angleDeg - angle0to360)
            val shortest = minOf(d, 360 - d)
            exposedAsbAngle = shortest

            // If minAngle == 0 => no limit, always accept closest
            if (minAngleFromAPointToActivateIt == 0 ||
                shortest <= minAngleFromAPointToActivateIt
            ) {
                p
            } else {
                null
            }
        }

        currentAction = selectedPoint
//        currentAction = if (dist > dragRadii[0]) selectedPoint else null

        hoveredAction = currentAction
        bannerVisible = currentAction != null
    } else if (!isDragging) {
        bannerVisible = false
        exposedClosest = null
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
                onLaunch(currentAction)
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
                    text = "closest point angle = ${exposedClosest?.angleDeg ?: "—"}",
                    color = Color.White, fontSize = 12.sp
                )
                Text(
                    text = "asb angle to closest point= $exposedAsbAngle",
                    color = Color.White, fontSize = 12.sp
                )
                Text(
                    text = "min angle gap = $minAngleFromAPointToActivateIt",
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

        val colorAction = if (hoveredAction != null) actionColor(hoveredAction!!.action, extraColors) else Color.Unspecified



        // Main drawing canva (the lines, circles and selected actions
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { // I use that to let the action in circle remove the background, otherwise it doesn't work
                    compositingStrategy = CompositingStrategy.Offscreen
                }
        ) {

            // Draw only if the user is dragging (has a start pos and a end (current) Offsets
            if (start != null && current != null) {


                // The line that goes from start dragging pos to the user's finger
                if (showAppLinePreview) {
                    drawCircle(
                        color = lineColor,
                        radius = circleRadius,
                        center = start,
                        style = Stroke(width = 3f)
                    )

                    if (!(linePreviewSnapToAction && hoveredAction != null)) {
                        actionLine(
                            drawScope = this,
                            start = start,
                            end = current,
                            radius = circleRadius,
                            color = lineColor
                        )
                    }
                }


                // The angle rotating around the start point (have to fix that and allow more customization) TODO
                // The "do you hate it" thing in settings
                if (showAppAnglePreview) {
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

                        // same circle radii as SettingsScreen
                        val radius = dragRadii[targetCircle -1]!!.toFloat()
                        // Main circle (the selected) drawn before any apps to be behind
                        if (showAppCirclePreview) {
                            drawCircle(
                                color = circleColor,
                                radius = radius,
                                center = start,
                                style = Stroke(4f)
                            )
                        }



                        // compute point position relative to origin
                        val px = start.x +
                                radius * sin(Math.toRadians(point.angleDeg)).toFloat()
                        val py = start.y -
                                radius * cos(Math.toRadians(point.angleDeg)).toFloat()


                        // If the user selected that the line has to snap to action, it is drawn here and not above
                        if (linePreviewSnapToAction) {
                            actionLine(
                                drawScope = this,
                                start = start,
                                end = Offset(px,py),
                                radius = circleRadius,
                                color = lineColor
                            )
                        }


                        // if you choose to draw every actions, they are drawn here, excepted for
                        // the selected one, that is always drawn last to prevent overlapping issues,
                        // even though it shouldn't happened due to my separatePoints functions
                        if (showAllActionsOnCurrentCircle) {
                            points.filter { it.nestId == nestId && it.circleNumber == targetCircle && it != point }.forEach { p ->
                                val px = start.x + radius * sin(Math.toRadians(p.angleDeg)).toFloat()
                                val py = start.y - radius * cos(Math.toRadians(p.angleDeg)).toFloat()

                                actionsInCircle(
                                    selected = false,
                                    drawScope = this,
                                    action = p.action!!,
                                    nests = nests,
                                    circleColor = circleColor,
                                    backgroundColor = null, // Null for now to erase bg (maybe settings later)
                                    colorAction = actionColor(p.action, extraColors),
                                    px = px, py = py,
                                    ctx = ctx,
                                    icons = icons,
                                    drawBorder = showActionIconBorder
                                )
                            }
                        }

                        // Draw here the actual selected action (if requested)
                        if (showAppLaunchPreview) {
                            actionsInCircle(
                                selected = true,
                                drawScope = this,
                                action = action,
                                nests = nests,
                                px = px, py = py,
                                ctx = ctx,
                                backgroundColor = null, // Null for now to erase bg (maybe settings later)
                                colorAction = colorAction,
                                circleColor = circleColor,
                                icons = icons,
                                drawBorder = showActionIconBorder
                            )
                        }
                    }
                }


                // Show the current selected app in the center of the circle
                if (showAppPreviewIconCenterStartPosition && hoveredAction != null) {
                    val currentAction = hoveredAction!!.action!!

                    actionsInCircle(
                        selected = false,
                        drawScope = this,
                        action = currentAction,
                        nests = nests,
                        px = start.x, py = start.y,
                        ctx = ctx,
                        drawBorder = false,
                        backgroundColor = null, // Null for now to erase bg (maybe settings later)
                        colorAction = colorAction,
                        circleColor = circleColor,
                        icons = icons
                    )
                }
            }
        }
    }



    // Label on top of the screen to indicate the launching app
    if (hoveredAction != null && (showLaunchingAppLabel || showLaunchingAppIcon)) {
        val currentAction = hoveredAction!!.action!!
        val label = actionLabel(currentAction)
        AppPreviewTitle(
            offsetY = offsetY,
            alpha = alpha,
            icons = icons,
            currentAction = currentAction,
            extraColors = extraColors,
            label = label,
            topPadding = 20.dp,
            showLabel = showLaunchingAppLabel,
            showIcon = showLaunchingAppIcon
        )
    }


    // Debug to test calendar and alarms opening
//    Row(
//        modifier = Modifier.fillMaxWidth()
//    ){
//        Button(
//            onClick = { openCalendar(ctx) },
//            colors = AppObjectsColors.buttonColors()
//        ) { Text("Test open calendar") }
//
//        Button(
//            onClick = { openAlarmApp(ctx) },
//            colors = AppObjectsColors.buttonColors()
//        ) { Text("Test open alarm") }
//    }
}

fun actionTint(action: SwipeActionSerializable, extraColors: ExtraColors): Color =
    when (action) {
        is SwipeActionSerializable.LaunchApp, SwipeActionSerializable.OpenDragonLauncherSettings  -> Color.Unspecified
        else -> actionColor(action, extraColors)
    }


private fun actionLine(
    drawScope: DrawScope,
    start: Offset,
    end: Offset,
    radius: Float,
    color: Color,
) {
    // Draw the main line from start to end
    drawScope.drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )

    // Erases the color, instead of putting it, that lets the wallpaper pass trough
    drawScope.drawCircle(
        color = Color.Transparent,
        radius = radius - 2,
        center = start,
        blendMode = BlendMode.Clear
    )

    // Small circle at the end of the trail
    drawScope.drawCircle(
        color = color,
        radius = 8f,
        center = end,
        style = Fill
    )
}
