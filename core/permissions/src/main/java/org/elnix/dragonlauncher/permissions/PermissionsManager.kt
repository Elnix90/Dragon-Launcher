package org.elnix.dragonlauncher.permissions

import android.app.Activity
import android.app.AppOpsManager
import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.github.elnix90.logging.logW
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.elnix.dragonlauncher.PERMISSIONS_TAG
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel
import org.elnix.dragonlauncher.ktx.tryStartActivity
import java.util.concurrent.atomic.AtomicReference

/**
 * Single source of truth for the permissions this launcher needs.
 *
 * Every group here is a "special" permission: it is granted outside the app, in
 * a system screen, a role request dialog or a runtime permission dialog. The
 * system never notifies the app when one of them changes, so a state cached at
 * startup silently goes stale.
 *
 * [PermissionsManagerImpl] therefore re-reads all groups every time the process
 * returns to the foreground and publishes the outcome, which is what makes the
 * state update in real time.
 */
public interface PermissionsManager {
	/**
	 * Sends the user to the system screen where [permissionGroup] is granted.
	 *
	 * No activity has to be provided: the manager tracks the foreground activity
	 * itself, so this is safe to call from a view model. No result is delivered
	 * back either, the grant is picked up by the automatic refresh on the way back.
	 */
	public fun requestPermission(permissionGroup: PermissionGroup)

	/**
	 * Reads [permissionGroup] straight from the system instead of returning the
	 * last published value.
	 *
	 * Use this to gate an action on the current truth, and [hasPermission] to
	 * observe it.
	 */
	public fun checkPermissionOnce(permissionGroup: PermissionGroup): Boolean

	/**
	 * The live granted state of [permissionGroup], republished on every foreground
	 * transition of the process.
	 *
	 * A [StateFlow] so collectors both observe changes and can read
	 * [StateFlow.value] synchronously when they need the latest known state.
	 */
	public fun hasPermission(permissionGroup: PermissionGroup): StateFlow<Boolean>
}

internal class PermissionsManagerImpl(
	private val ctx: Context
) : PermissionsManager {
	/**
	 * One state holder per group, keyed by the group itself.
	 *
	 * Deriving the holders from [PermissionGroup.entries] keeps the published
	 * state, the checks and the groups in sync without repeating the list.
	 */
	private val states: Map<PermissionGroup, MutableStateFlow<Boolean>> =
		PermissionGroup.entries.associateWith { MutableStateFlow(checkPermissionOnce(it)) }

	/**
	 * The activity currently in the foreground, needed to show the runtime
	 * permission dialogs. Held weakly so the manager, which is a singleton, can
	 * never keep a destroyed activity alive.
	 */
	private val foregroundActivity = AtomicReference<Activity?>(null)

	init {
		// A view model only has the application, so the manager resolves the
		// activity it needs to request a runtime permission. Settings intents would
		// work from the application context, but runtime dialogs cannot.
		(ctx.applicationContext as? Application)?.registerActivityLifecycleCallbacks(
			ForegroundActivityTracker(foregroundActivity)
		)

		// Resuming is the only signal the system gives us after a grant, so this is
		// the single point that keeps the state fresh. ON_RESUME covers both cases
		// we care about: returning from a settings screen, a role request or a
		// runtime permission dialog, and it is also dispatched on cold start.
		// ON_START is handled as well so a resume is never missed if the manager is
		// created while the process is already foregrounded. Re-reading is cheap and
		// StateFlow drops unchanged values, so extra calls cost nothing.
		ProcessLifecycleOwner
			.get()
			.lifecycle
			.addObserver(
				LifecycleEventObserver { _, event ->
					if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
						refresh()
					}
				}
			)
	}

	override fun hasPermission(permissionGroup: PermissionGroup): StateFlow<Boolean> =
		states.getValue(permissionGroup).asStateFlow()

	override fun requestPermission(permissionGroup: PermissionGroup) {
		val settingsIntent =
			when (permissionGroup) {
				PermissionGroup.Notifications -> {
					Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
				}

				PermissionGroup.Accessibility -> {
					Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
				}

				PermissionGroup.UsageStat -> {
					Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
				}

				// Both groups depend on being the default launcher, so they share
				// the home role request. It is unavailable below Q and can be null
				// when the role is not declared, in which case the generic screen
				// still lets the user pick the default launcher by hand.
				PermissionGroup.AppShortcuts,
				PermissionGroup.DefaultLauncher -> {
					Intent(Settings.ACTION_HOME_SETTINGS)
				}
			}

		// NEW_TASK lets the application context start the screen, so a request
		// works even when the caller is not an activity.
		settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

		if (!ctx.tryStartActivity(settingsIntent)) {
			logW(PERMISSIONS_TAG) { "No system screen available to request $permissionGroup" }
		}
	}

	override fun checkPermissionOnce(permissionGroup: PermissionGroup): Boolean =
		when (permissionGroup) {
			PermissionGroup.AppShortcuts -> {
				ctx
					.getSystemService<LauncherApps>()
					?.hasShortcutHostPermission() == true
			}

			// Both groups are granted by holding the home role, so the check is
			// shared. Below Q there is no role, and the default home app is resolved
			// the only way the platform exposes.
			PermissionGroup.DefaultLauncher -> {
				isDefaultLauncher()
			}

			PermissionGroup.UsageStat -> {
				hasUsageStatsPermission()
			}

			// Both are granted in a system screen and recorded only there, so the
			// secure setting is the source of truth. Reading it directly also makes
			// the state correct before any of our services has been bound, which a
			// report from the service itself could not guarantee.
			PermissionGroup.Notifications -> {
				isComponentEnabled(ENABLED_NOTIFICATION_LISTENERS)
			}

			PermissionGroup.Accessibility -> {
				isComponentEnabled(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
			}
		}

	/**
	 * Re-reads every group and publishes the results.
	 *
	 * [MutableStateFlow] drops writes that leave the value unchanged, so
	 * collectors are only woken up on a real grant or a real revocation.
	 */
	private fun refresh() {
		states.forEach { (group, state) ->
			state.value = checkPermissionOnce(group)
		}
	}

	/**
	 * Whether any component of this package is listed under the given
	 * [Settings.Secure] key.
	 *
	 * The stored value is a colon separated list whose entries are flattened
	 * `package/class` components on most systems, but some versions and OEM skins
	 * store bare package names, so only the package part is compared.
	 */
	private fun isComponentEnabled(key: String): Boolean =
		Settings.Secure
			.getString(
				ctx.contentResolver,
				key
			).orEmpty()
			.split(':')
			.any { it.substringBefore('/').equals(ctx.packageName, ignoreCase = true) }

	/**
	 * Whether this app is the user default home app.
	 *
	 * The role is the supported way of asking from Q on. Below it the only option
	 * is to resolve the home intent and see who handles it.
	 */
	private fun isDefaultLauncher(): Boolean =
		if (isAtLeastApiLevel(Build.VERSION_CODES.Q)) {
			ctx.getSystemService<RoleManager>()?.isRoleHeld(RoleManager.ROLE_HOME) == true
		} else {
			val homeIntent =
				Intent(Intent.ACTION_MAIN).apply {
					addCategory(Intent.CATEGORY_HOME)
				}

			ctx.packageManager
				.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
				?.activityInfo
				?.packageName == ctx.packageName
		}

	/**
	 * Usage access is an app op rather than a runtime permission, so it has to be
	 * read through [AppOpsManager].
	 *
	 * [AppOpsManager.checkOpNoThrow] reports the mode without throwing when the op
	 * is not allowed, and only [AppOpsManager.MODE_ALLOWED] counts as granted. The
	 * `unsafeCheckOpNoThrow` variants are deprecated and are not needed here, they
	 * only skip the mode resolution the framework already performs.
	 */
	private fun hasUsageStatsPermission(): Boolean {
		val appOps = ctx.getSystemService<AppOpsManager>() ?: return false

		return appOps.checkOpNoThrow(
			AppOpsManager.OPSTR_GET_USAGE_STATS,
			Process.myUid(),
			ctx.packageName
		) == AppOpsManager.MODE_ALLOWED
	}

	/**
	 * Keeps [foregroundActivity] pointing at the activity that can show a dialog.
	 */
	private class ForegroundActivityTracker(
		private val current: AtomicReference<Activity?>
	) : Application.ActivityLifecycleCallbacks {
		override fun onActivityStarted(activity: Activity) {
			current.set(activity)
		}

		override fun onActivityStopped(activity: Activity) {
			// Only clear when this is still the tracked one, a rotation or a dialog
			// can stop an activity that has already been replaced.
			current.compareAndSet(activity, null)
		}

		override fun onActivityCreated(
			activity: Activity,
			savedInstanceState: android.os.Bundle?
		) {
		}

		override fun onActivityResumed(activity: Activity) {
			current.set(activity)
		}

		override fun onActivityPaused(activity: Activity) {
		}

		override fun onActivitySaveInstanceState(
			activity: Activity,
			outState: android.os.Bundle
		) {
		}

		override fun onActivityDestroyed(activity: Activity) {
			current.compareAndSet(activity, null)
		}
	}

	private companion object {
		/**
		 * Key of the secure setting listing the allowed notification listeners.
		 *
		 * Spelled out because AOSP keeps `Settings.Secure.ENABLED_NOTIFICATION_LISTENERS`
		 * hidden, so it is not part of the public SDK. The value is part of the
		 * platform contract and has never changed.
		 */
		private const val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
	}
}

/**
 * Lets non injected code reach the manager.
 *
 * The permission checks are needed from plain objects and top level functions
 * that cannot receive a dependency, for example the accessibility helpers. Going
 * through an entry point keeps the manager the only implementation of a check
 * instead of duplicating the platform query at every call site.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface PermissionsManagerEntryPoint {
	fun permissionsManager(): PermissionsManager
}

/**
 * The shared [PermissionsManager], reachable from any context.
 */
public val Context.permissionsManager: PermissionsManager
	get() = EntryPointAccessors
		.fromApplication(
			applicationContext,
			PermissionsManagerEntryPoint::class.java
		).permissionsManager()
