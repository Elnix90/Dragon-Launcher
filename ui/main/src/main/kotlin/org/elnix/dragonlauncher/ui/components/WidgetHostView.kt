@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package org.elnix.dragonlauncher.ui.components

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Widget
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.getCenter
import org.elnix.dragonlauncher.ktx.toDp
import org.elnix.dragonlauncher.models.WidgetsViewModel
import org.elnix.dragonlauncher.ui.actions.ActionIcon
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.actions.ShortcutIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.helpers.swipe.PointIcon
import org.elnix.dragonlauncher.ui.widgets.LauncherWidgetHolder
import kotlin.math.min


@Composable
fun WidgetHostView(
    widget: Widget,
    cellSizePx: Float,
    modifier: Modifier = Modifier,
    blockTouches: Boolean = false,
    widgetsViewModel: WidgetsViewModel = activityViewModel(),
    onLaunchAction: () -> Unit
) {
    val ctx = LocalContext.current
    val density = LocalDensity.current.density
    val currentView = LocalView.current


    if (widget.action is Action.OpenWidget) {
        val launcherWidgetHolder = remember(ctx) { LauncherWidgetHolder.getInstance(ctx) }
        val appWidgetId = widget.appWidgetId

        val hostView = remember(appWidgetId, currentView) {
            if (appWidgetId == null) return@remember null
            val info = launcherWidgetHolder.getAppWidgetInfo(appWidgetId)
            if (info != null) {
                launcherWidgetHolder.createView(appWidgetId, info)
            } else null
        } ?: run {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.large
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.question_mark),
                        contentDescription = stringResource(R.string.widget_not_found),
                    )
                    Spacer(5.dp)
                    Text(
                        text = stringResource(R.string.widget_not_found),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

        // Apply size options when span changes
        DisposableEffect(widget.spanX, widget.spanY) {
            val widthDp = (widget.spanX * cellSizePx / density).toInt()
            val heightDp = (widget.spanY * cellSizePx / density).toInt()

            val options = Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp)
            }
            // Non nullability ensured by the remember block that returns if its is null
            launcherWidgetHolder.updateAppWidgetOptions(appWidgetId!!, options)
            onDispose { }
        }

        AndroidView(
            modifier = modifier
                .fillMaxSize()
                .clip(widget.shape.resolveShape(default = IconShape.RightSquare))
                .pointerInteropFilter { blockTouches },
            factory = {
                // Remove from previous parent if any (Compose safe re-attachment)
                (hostView.parent as? ViewGroup)?.removeView(hostView)

                hostView.setPadding(0, 0, 0, 0)

                FrameLayout(it).apply {
                    clipChildren = true
                    clipToPadding = true
                    addView(
                        hostView,
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                }
            },
            update = {
                // Visual update if needed (re-bind is handled by HostView updates)
            }
        )
    } else {
        val sizeDp = min((widget.spanX * cellSizePx), (widget.spanY * cellSizePx)).toDp

        when (val action = widget.action) {
            is Action.LaunchApp -> {
                val app by widgetsViewModel.findOne(action).collectAsStateWithLifecycle(null)
                app?.let {
                    AppIcon(
                        app = it,
                        size = sizeDp,
                        modifier = modifier.conditional(!blockTouches) {
                            clickable(onClick = onLaunchAction)
                        }
                    )
                }
            }

            is Action.OpenNest -> {
                val editPoint = Point(
                    offset = Offset.Zero,
                    action = Action.OpenNest(action.nestId),
                    id = -2
                )

                BoxWithConstraints(
                    modifier = modifier
                        .size(sizeDp)
                        .clip(widget.shape.resolveShape(default = IconShape.RightSquare))
                        .conditional(!blockTouches) {
                            clickable(onClick = onLaunchAction)
                        },
                ) {
                    val center = constraints.getCenter()

                    PointIcon(
                        selected = false,
                        eraseColor = Color.Transparent,
                        point = editPoint,
                        center = center
                    )
                }
            }

            is Action.LaunchShortcut -> {
                ShortcutIcon(
                    shortcut = action,
                    size = sizeDp,
                    modifier = modifier.conditional(!blockTouches) {
                        clickable(onClick = onLaunchAction)
                    }
                )
            }

            else -> {
                ActionIcon(
                    action = action,
                    size = sizeDp,
                    modifier = modifier
                        .fillMaxSize()
                        .clip(widget.shape.resolveShape(default = IconShape.RightSquare))
                        .conditional(!blockTouches) {
                            clickable(onClick = onLaunchAction)
                        }
                )
            }
        }
    }
}
