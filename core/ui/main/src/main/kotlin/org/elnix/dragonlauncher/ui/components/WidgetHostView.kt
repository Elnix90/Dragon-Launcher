@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package org.elnix.dragonlauncher.ui.components

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Widget
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.ktx.getCenter
import org.elnix.dragonlauncher.ktx.toDp
import org.elnix.dragonlauncher.ui.actions.ActionIcon
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.helpers.nests.PointIcon
import org.elnix.dragonlauncher.ui.widgets.LauncherWidgetHolder
import kotlin.math.min


@Composable
fun WidgetHostView(
    widget: Widget,
    cellSizePx: Float,
    modifier: Modifier = Modifier,
    blockTouches: Boolean = false,
    onLaunchAction: () -> Unit
) {
    val ctx = LocalContext.current
    val density = LocalDensity.current.density
    val currentView = LocalView.current


    if (widget.action is Action.OpenWidget) {
        val launcherWidgetHolder = remember(ctx) { LauncherWidgetHolder.getInstance(ctx) }
        val appWidgetId = widget.appWidgetId ?: (widget.action as Action.OpenWidget).widgetId

        val hostView = remember(appWidgetId, currentView) {
            val info = launcherWidgetHolder.getAppWidgetInfo(appWidgetId)
            if (info != null) {
                launcherWidgetHolder.createView(appWidgetId, info)
            } else {
                null
            }
        } ?: return

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
            launcherWidgetHolder.updateAppWidgetOptions(appWidgetId, options)
            onDispose { }
        }

        AndroidView(
            modifier = modifier
                .fillMaxSize()
                .clip(widget.shape.resolveShape(default = IconShape.Square))
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

        if (widget.action !is Action.OpenCircleNest) {
            ActionIcon(
                action = widget.action,
                modifier = modifier
                    .fillMaxSize()
                    .clip(widget.shape.resolveShape(default = IconShape.Square))
                    .conditional(!blockTouches) {
                        clickable { onLaunchAction() }
                    },
                size = sizeDp
            )
        } else {
            val editPoint = Point(
                offset = Offset.Zero,
                action = Action.OpenCircleNest((widget.action as Action.OpenCircleNest).nestId),
                id = -2
            )

            BoxWithConstraints(
                modifier = modifier
                    .size(sizeDp)
                    .clip(widget.shape.resolveShape(default = IconShape.Square))
                    .conditional(!blockTouches) {
                        clickable { onLaunchAction() }
                    },
            ) {
                val center = constraints.getCenter()

                PointIcon(
                    selected = false,
                    point = editPoint,
                    center = center,
                    depth = 1,
                )
            }
        }
    }
}
