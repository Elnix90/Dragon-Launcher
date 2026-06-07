package org.elnix.dragonlauncher.ui.widgets

import android.annotation.SuppressLint
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import org.elnix.dragonlauncher.logging.LAUNCHER_WIDGET_HOLDER_TAG
import org.elnix.dragonlauncher.logging.WIDGET_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import java.lang.ref.WeakReference

/**
 * A robust wrapper for AppWidgetHost, inspired by Lawnchair's LauncherWidgetHolder.
 * Provides better lifecycle management and view recycling.
 */
class LauncherWidgetHolder(private val ctx: Context) : DefaultLifecycleObserver {

    companion object {
        private const val HOST_ID = 1024

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: LauncherWidgetHolder? = null

        fun getInstance(ctx: Context): LauncherWidgetHolder {
            return instance ?: synchronized(this) {
                instance ?: LauncherWidgetHolder(ctx.applicationContext).also { instance = it }
            }
        }
    }

    private val appWidgetManager = AppWidgetManager.getInstance(ctx)
    private val appWidgetHost = object : AppWidgetHost(ctx, HOST_ID) {
        override fun onCreateView(
            ctx: Context,
            appWidgetId: Int,
            appWidget: AppWidgetProviderInfo?
        ): AppWidgetHostView {
            return DragonAppWidgetHostView(ctx)
        }
    }

    private val views = SparseArray<WeakReference<AppWidgetHostView>>()
    private var isListening = false

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        startListening()
    }

    override fun onStop(owner: LifecycleOwner) {
        stopListening()
    }

    fun startListening() {
        if (!isListening) {
            try {
                appWidgetHost.startListening()
                isListening = true
                logD(LAUNCHER_WIDGET_HOLDER_TAG) { "AppWidgetHost started listening" }
            } catch (e: Exception) {
                logE(LAUNCHER_WIDGET_HOLDER_TAG, e) { "Failed to start listening" }
            }
        }
    }

    fun stopListening() {
        if (isListening) {
            try {
                appWidgetHost.stopListening()
                isListening = false
                logD(LAUNCHER_WIDGET_HOLDER_TAG) { "AppWidgetHost stopped listening" }
            } catch (e: Exception) {
                logE(LAUNCHER_WIDGET_HOLDER_TAG, e) { "Failed to stop listening" }
            }
        }
    }

    fun allocateAppWidgetId(): Int {
        val id = appWidgetHost.allocateAppWidgetId()
        logD(WIDGET_TAG) { "DRAGON_WIDGET: Allocated new ID: $id" }
        return id
    }

//    fun bindWidget(appWidgetId: Int, provider: android.content.ComponentName, options: Bundle? = null): Boolean {
//        logD(WIDGET_TAG) { "DRAGON_WIDGET: Starting bind for ID $appWidgetId with provider $provider" }
//        val result = appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, provider, options)
//        logD(WIDGET_TAG) { "DRAGON_WIDGET: bindAppWidgetIdIfAllowed result for ID $appWidgetId: $result" }
//        return result
//    }

    fun deleteAppWidgetId(appWidgetId: Int) {
        logD(WIDGET_TAG) { "DRAGON_WIDGET: Deleting ID $appWidgetId" }
        appWidgetHost.deleteAppWidgetId(appWidgetId)
        views.remove(appWidgetId)
    }

    fun createView(appWidgetId: Int, info: AppWidgetProviderInfo): AppWidgetHostView {
        logD(WIDGET_TAG) { "DRAGON_WIDGET: Creating view for ID $appWidgetId (Provider: ${info.provider})" }
        val view = appWidgetHost.createView(ctx, appWidgetId, info)
        views.put(appWidgetId, WeakReference(view))
        return view
    }

    fun getAppWidgetInfo(appWidgetId: Int): AppWidgetProviderInfo? {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
        logD(WIDGET_TAG) { "DRAGON_WIDGET: getAppWidgetInfo for ID $appWidgetId: ${info?.provider ?: "NULL"}" }
        return info
    }

    fun updateAppWidgetOptions(appWidgetId: Int, options: Bundle) {
        appWidgetManager.updateAppWidgetOptions(appWidgetId, options)
    }

    /**
     * Custom AppWidgetHostView that handles common launcher requirements.
     */
    class DragonAppWidgetHostView(ctx: Context) : AppWidgetHostView(ctx) {
        override fun getErrorView(): android.view.View {
            // Can be customized to show a "Lawnchair-style" error layout
            return super.getErrorView()
        }
    }
}
