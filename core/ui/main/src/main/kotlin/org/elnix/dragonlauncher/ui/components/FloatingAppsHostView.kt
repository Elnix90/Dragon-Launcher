@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package org.elnix.dragonlauncher.ui.components

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.center
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import org.elnix.dragonlauncher.common.serializables.FloatingAppObject
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.common.messyfolder.resolveShape
import org.elnix.dragonlauncher.ui.actions.ActionIcon
import org.elnix.dragonlauncher.ui.helpers.nests.actionsInCircle
import org.elnix.dragonlauncher.ui.remembers.rememberSwipeDefaultParams
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.widgets.LauncherWidgetHolder
import kotlin.math.min


@Composable
fun FloatingAppsHostView(
    floatingAppObject: FloatingAppObject,
    cellSizePx: Float,
    modifier: Modifier = Modifier,
    blockTouches: Boolean = false,
    onLaunchAction: () -> Unit
) {
    val ctx = LocalContext.current
    val density = LocalDensity.current.density
    val currentView = LocalView.current


    if (floatingAppObject.action is SwipeActionSerializable.OpenWidget) {
        val launcherWidgetHolder = remember(ctx) { LauncherWidgetHolder.getInstance(ctx) }
        val appWidgetId = floatingAppObject.appWidgetId ?: (floatingAppObject.action as SwipeActionSerializable.OpenWidget).widgetId

        val hostView = remember(appWidgetId, currentView) {
            val info = launcherWidgetHolder.getAppWidgetInfo(appWidgetId)
            if (info != null) {
                launcherWidgetHolder.createView(appWidgetId, info)
            } else {
                null
            }
        } ?: return

        // Apply size options when span changes
        DisposableEffect(floatingAppObject.spanX, floatingAppObject.spanY) {
            val widthDp = (floatingAppObject.spanX * cellSizePx / density).toInt()
            val heightDp = (floatingAppObject.spanY * cellSizePx / density).toInt()

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
                .clip(floatingAppObject.shape.resolveShape(default = IconShape.Square))
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
        val sizePx = min((floatingAppObject.spanX * cellSizePx), (floatingAppObject.spanY * cellSizePx)).toInt()

        if (floatingAppObject.action !is SwipeActionSerializable.OpenCircleNest) {
            ActionIcon(
                action = floatingAppObject.action,
                modifier = modifier
                    .fillMaxSize()
                    .clip(floatingAppObject.shape.resolveShape(default = IconShape.Square))
                    .conditional(!blockTouches) {
                        clickable { onLaunchAction() }
                    },
                size = sizePx
            )
        } else {

            val sizeDp = with(LocalDensity.current) { sizePx.toDp() }
            val drawParams = rememberSwipeDefaultParams()

            val editPoint = SwipePointSerializable(
                circleNumber = 0,
                angleDeg = 0.0,
                action = SwipeActionSerializable.OpenCircleNest((floatingAppObject.action as SwipeActionSerializable.OpenCircleNest).nestId),
                id = ""
            )

            Canvas(
                modifier = modifier
                    .size(sizeDp)
                    .clip(floatingAppObject.shape.resolveShape(default = IconShape.Square))
                    .conditional(!blockTouches) {
                        clickable { onLaunchAction() }
                    },
            ) {
                val center = this.size.center

                actionsInCircle(
                    selected = false,
                    point = editPoint,
                    center = center,
                    depth = 1,
                    drawParams = drawParams
                )
            }
        }
    }
}
