package org.elnix.dragonlauncher.base

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo

interface WidgetHostProvider {
	fun createAppWidgetView(widgetId: Int): AppWidgetHostView?

	fun getAppWidgetInfo(widgetId: Int): AppWidgetProviderInfo?
}
