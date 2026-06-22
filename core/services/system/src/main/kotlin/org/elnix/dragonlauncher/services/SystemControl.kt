package org.elnix.dragonlauncher.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import io.github.elnix90.logging.logE
import org.elnix.dragonlauncher.ktx.showToast
import io.github.elnix90.logging.ACCESSIBILITY_TAG

object SystemControl {


    fun isServiceEnabled(ctx: Context): Boolean {
        val enabled = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabled.contains(ctx.packageName)
    }

    fun openServiceSettings(ctx: Context) {
        ctx.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    /**
     * Called by SystemControlService.onCreate() to store a static instance.
     */
    fun attachInstance(service: SystemControlService) {
        SystemControlService.INSTANCE = service
    }

    fun expandNotifications() {
        SystemControlService.INSTANCE?.openNotificationShade()
    }


    fun expandQuickSettings(ctx: Context) {
        try {
            val statusBarService = ctx.getSystemService("statusbar")
            val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
            val method = statusBarManagerClass.getMethod("expandSettingsPanel")
            method.invoke(statusBarService)
        } catch (e: Exception) {
            logE(ACCESSIBILITY_TAG, e) { "Reflection failed" }
            // Fallback to notifications if quick settings fails
            expandNotifications()
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    fun lockScreen(ctx: Context) {
        if (!isServiceEnabled(ctx)) {
            openServiceSettings(ctx)
            return
        }
        SystemControlService.INSTANCE?.performGlobalAction(
            AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
        )
    }

    fun openRecentApps(ctx: Context) {
        if (!isServiceEnabled(ctx)) {
            ctx.showToast("Please enable accessibility settings to use that feature")
            openServiceSettings(ctx)
            return
        }
        SystemControlService.INSTANCE?.openRecentApps()
    }


    fun launchDragon(ctx: Context) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            setPackage(ctx.packageName)
        }
        try {
            ctx.startActivity(intent)
        } catch (e: Exception) {
            logE(ACCESSIBILITY_TAG, e) { "Launch failed" }
            ctx.showToast("Failed to launch Dragon Launcher")
        }
    }
}
