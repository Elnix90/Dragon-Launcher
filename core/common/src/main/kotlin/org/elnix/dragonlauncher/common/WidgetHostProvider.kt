package org.elnix.dragonlauncher.common

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo

public interface WidgetHostProvider {
    public fun createAppWidgetView(widgetId: Int): AppWidgetHostView?
    public fun getAppWidgetInfo(widgetId: Int): AppWidgetProviderInfo?
}
