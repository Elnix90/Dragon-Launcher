package org.elnix.dragonlauncher.models

import android.content.pm.LauncherApps
import android.os.Bundle
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.elnix90.logging.logE
import io.github.elnix90.logging.logI
import io.github.elnix90.logging.logW
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.elnix.dragonlauncher.APP_LAUNCH_TAG
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.compat.PackageManagerCompat
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.PermissionsManager
import org.elnix.dragonlauncher.profiles.ProfileManager
import org.elnix.dragonlauncher.recents.RecentsService
import org.elnix.dragonlauncher.settings.stores.map.WellbeingSettingsStore
import org.elnix.dragonlauncher.timer.AppTimerService
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds


@Stable
@HiltViewModel
public class AppLaunchViewModel @Inject constructor(
    application: android.app.Application,
    permissionsManager: PermissionsManager,
    private val recentsService: RecentsService,
    private val profileManager: ProfileManager,
    private val packageManagerCompat: PackageManagerCompat,
    private val appRepository: AppRepository
) : AndroidViewModel(application) {

    public val hasUsageStatsPermission: StateFlow<Boolean> = permissionsManager.hasPermission(PermissionGroup.UsageStat).stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        false
    )

    public val pendingAppLaunch: SettingFlow<Application?> = SettingFlow(null)

    private var currentLaunchJob: Job? = null


    public fun requestAppLaunch(launchAction: Action.LaunchApp) {
        viewModelScope.launch {
            // Resolve the stored profile to the live one, as the persisted userHandle
            // may have been serialized incorrectly (e.g. by older versions).
            val profile = profileManager.resolveProfile(launchAction.profile)
            if (profile == null) {
                logW(APP_LAUNCH_TAG) { "Profile ${launchAction.profile} not found for ${launchAction.packageName}" }
                return@launch
            }

            val app = appRepository.findOne(launchAction).first()
            if (app != null) {
                requestAppLaunch(app)
            } else {
                launchLockedApp(profile, launchAction)
            }
        }
    }

    /**
     * Requests an unlock of the given profile and launches the app once it's
     * available. The wait for the unlock is unbounded: it lasts as long as it
     * takes the user to confirm the unlock dialog. Only the wait for the app to
     * appear in the app list afterward is bounded.
     */
    private fun launchLockedApp(profile: Profile, action: Action.LaunchApp) {
        if (!isAtLeastApiLevel(28) || !profileManager.isProfileLocked(profile)) {
            logW(APP_LAUNCH_TAG) { "App ${action.packageName} not available and ${profile.type} profile is not locked" }
            return
        }

        logI(APP_LAUNCH_TAG) { "Unlocking ${profile.type} profile to launch ${action.packageName}" }
        profileManager.unlockProfile(profile)

        viewModelScope.launch {
            if (!waitForProfileUnlock(profile)) return@launch
            val app = waitForAppAvailability(profile, action) ?: return@launch
            requestAppLaunch(app)
        }
    }

    public fun requestAppLaunch(app: Application) {
        viewModelScope.launch {
            val startAppTimer =
                if (!WellbeingSettingsStore.socialMediaPauseEnabled.get(application)) false
                else app.packageName in WellbeingSettingsStore.pausedApps.get(application)

            if (startAppTimer) {
                pendingAppLaunch.value = app
                return@launch
            }

            launchAppWithProfileUnlock(app)
        }
    }


    public suspend fun startTimer(timeLimitMinutes: Int?, app: Application) {
        AppTimerService.start(
            ctx = application,
            application = app,
            reminderEnabled = WellbeingSettingsStore.reminderEnabled.flow(application).first(),
            reminderIntervalMinutes = WellbeingSettingsStore.reminderIntervalMinutes.flow(application).first(),
            reminderMode = WellbeingSettingsStore.reminderMode.flow(application).first().toString(),
            timeLimitMinutes = timeLimitMinutes
        )
    }

    public fun onAppTimerServiceStarted(duration: Int?): Boolean {
        val pendingApp = pendingAppLaunch.value
        if (pendingApp != null) {

            if (duration != null) {
                viewModelScope.launch {
                    startTimer(duration, pendingApp)
                }
            }

            launchAppDirectly(pendingApp)
            return true
        }
        return false
    }

    public fun launchShortcut(action: Action.LaunchShortcut) {
        action.takeIf { it.packageName.isNotEmpty() }?.let {
            packageManagerCompat.launchShortcut(it.packageName, it.shortcutId)
        }
    }

    private fun launchAppWithProfileUnlock(app: Application) {
        currentLaunchJob?.cancel()
        currentLaunchJob = viewModelScope.launch {
            if (isAtLeastApiLevel(28) && profileManager.isProfileLocked(app.profile)) {
                logI(APP_LAUNCH_TAG) { "Unlocking ${app.profile.type} profile to launch ${app.packageName}" }
                profileManager.unlockProfile(app.profile)
                if (!waitForProfileUnlock(app.profile)) return@launch
            }
            launchAppDirectly(app)
        }
    }

    private suspend fun waitForProfileUnlock(profile: Profile): Boolean = try {
        profileManager.getProfileState(profile).first { it?.locked == false }
        true
    } catch (e: CancellationException) {
        logE(APP_LAUNCH_TAG, e) { "App launch canceled while waiting for profile unlock" }
        false
    }

    private suspend fun waitForAppAvailability(profile: Profile, action: Action.LaunchApp): Application? {
        return withTimeoutOrNull(15_000L.milliseconds) {
            val modifiedAction = action.copy(profile = profile)
            appRepository.findOne(modifiedAction).first { it != null }
        } ?: run {
            logW(APP_LAUNCH_TAG) { "App ${action.packageName} still not available after ${profile.type} profile unlock" }
            null
        }
    }


    /**
     * Launch an app directly without any pause check.
     * Used both by launchAction and after the digital pause screen.
     */
    private fun launchAppDirectly(app: Application) {
        val launcherApps = application.getSystemService(LauncherApps::class.java)

        val packageName = app.packageName

        val activity = launcherApps
            .getActivityList(null, app.profile.userHandle)
            .firstOrNull { it.applicationInfo.packageName == packageName }
            ?: run {
                logW(APP_LAUNCH_TAG) { "Launcher activity not found for $packageName" }
                return
            }

        val options = Bundle()

        if (isAtLeastApiLevel(31)) {
            options.putInt("android.activity.splashScreenStyle", 1)
        }

        try {
            launcherApps.startMainActivity(
                activity.componentName,
                app.profile.userHandle,
                null,
                options
            )

            recentsService.touch(app)

        } catch (e: SecurityException) {
            logE(APP_LAUNCH_TAG, e) { "Security error launching $packageName" }
        } catch (e: NullPointerException) {
            logE(APP_LAUNCH_TAG, e) { "App component not found for $packageName" }
        } catch (e: Exception) {
            logE(APP_LAUNCH_TAG, e) { "Failed to launch $packageName" }
        }
    }


    init {
        viewModelInitialized()
    }
}
