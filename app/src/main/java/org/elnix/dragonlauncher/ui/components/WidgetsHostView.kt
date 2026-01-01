package org.elnix.dragonlauncher.ui.components

import android.appwidget.AppWidgetManager
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.elnix.dragonlauncher.MainActivity
import org.elnix.dragonlauncher.data.helpers.WidgetInfo

/**
 * Displays a bound AppWidget inside Compose using AndroidView.
 *
 * This composable assumes that:
 * - The widget ID is already allocated
 * - The widget is already bound
 * - The widget configuration (if required) has completed successfully
 *
 * It is safe against recomposition and prevents the "child already has a parent" crash.
 */
@Composable
fun WidgetHostView(
    widgetInfo: WidgetInfo,
    modifier: Modifier = Modifier,
    blockTouches: Boolean = false
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
        ?: error("WidgetHostView must be hosted inside MainActivity")

    val appWidgetManager = remember {
        AppWidgetManager.getInstance(context)
    }

    val hostView = remember(widgetInfo.id) {
        val info = appWidgetManager.getAppWidgetInfo(widgetInfo.id)
            ?: return@remember null

        activity.appWidgetHost.createView(context, widgetInfo.id, info).apply {
            setAppWidget(widgetInfo.id, info)
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
        }
    )
}
