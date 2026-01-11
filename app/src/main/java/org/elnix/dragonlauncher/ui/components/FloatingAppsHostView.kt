@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package org.elnix.dragonlauncher.ui.components

import android.appwidget.AppWidgetManager
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.elnix.dragonlauncher.MainActivity
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.helpers.FloatingAppObject
import org.elnix.dragonlauncher.utils.actions.ActionIcon


@Composable
fun FloatingAppsHostView(
    floatingAppObject: FloatingAppObject,
    icons: Map<String, ImageBitmap>,
    cellSizePx: Float,
    modifier: Modifier = Modifier,
    blockTouches: Boolean = false,
    onLaunchAction: () -> Unit
) {
    val ctx = LocalContext.current


    if (floatingAppObject.action is SwipeActionSerializable.OpenWidget) {
        val activity = ctx as? MainActivity
            ?: error("WidgetHostView must be hosted inside MainActivity")

        val appWidgetManager = remember {
            AppWidgetManager.getInstance(ctx)
        }

        val hostView = remember(floatingAppObject.action.widgetId) {
            val info = appWidgetManager.getAppWidgetInfo(floatingAppObject.action.widgetId)
                ?: return@remember null

            activity.appWidgetHost.createView(ctx, floatingAppObject.action.widgetId, info).apply {
                setAppWidget(floatingAppObject.action.widgetId, info)
            }
        } ?: return

        AndroidView(
            modifier = modifier
                .fillMaxSize()
                .pointerInteropFilter { blockTouches },
            factory = {
                FrameLayout(it).apply {
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
                val info = appWidgetManager.getAppWidgetInfo(floatingAppObject.id)
                if (info != null) {
                    hostView.setAppWidget(floatingAppObject.id, info)
                }
            }
        )
    } else {
        val size = kotlin.math.min((floatingAppObject.spanX * cellSizePx), (floatingAppObject.spanY * cellSizePx)).toInt()

        ActionIcon(
            action = floatingAppObject.action,
            icons = icons,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .let { mod ->
                    if (blockTouches) { mod } else { mod.clickable{ onLaunchAction() } }
                },
            size = size
        )
    }
}
