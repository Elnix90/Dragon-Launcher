package org.elnix.dragonlauncher.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import io.github.elnix90.logging.logE
import org.elnix.dragonlauncher.ACCESSIBILITY_TAG
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.permissionsManager

object SystemControl {
	/**
	 * Whether the accessibility service is granted.
	 *
	 * Delegates to [org.elnix.dragonlauncher.permissions.PermissionsManager], which owns every permission check in the
	 * app. The comparison there matches the whole component name, unlike the
	 * substring match done here, which also matched on partial package names.
	 */
	fun isServiceEnabled(ctx: Context): Boolean =
		ctx.permissionsManager.checkPermissionOnce(PermissionGroup.Accessibility)

	/**
	 * Opens the accessibility settings screen, through the manager so the request
	 * path is the same one the permissions screen uses.
	 */
	fun openServiceSettings(ctx: Context) {
		ctx.permissionsManager.requestPermission(PermissionGroup.Accessibility)
	}

	/**
	 * Called by SystemControlService.onCreate() to store a static instance.
	 */
	fun attachInstance(service: SystemControlService) {
		SystemControlService.instance = service
	}

	fun expandNotifications() {
		SystemControlService.instance?.openNotificationShade()
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
		SystemControlService.instance?.performGlobalAction(
			AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
		)
	}

	fun openRecentApps(ctx: Context) {
		if (!isServiceEnabled(ctx)) {
			ctx.showToast("Please enable accessibility settings to use that feature")
			openServiceSettings(ctx)
			return
		}
		SystemControlService.instance?.openRecentApps()
	}

	fun launchDragon(ctx: Context) {
		val intent =
			Intent(Intent.ACTION_MAIN).apply {
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
