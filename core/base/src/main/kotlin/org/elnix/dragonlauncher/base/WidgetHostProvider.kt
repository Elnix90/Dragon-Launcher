package org.elnix.dragonlauncher.base

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo

public interface WidgetHostProvider {
    public fun createAppWidgetView(widgetId: Int): AppWidgetHostView?

    public fun getAppWidgetInfo(widgetId: Int): AppWidgetProviderInfo?
}
