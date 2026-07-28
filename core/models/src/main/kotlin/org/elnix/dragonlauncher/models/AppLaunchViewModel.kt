package org.elnix.dragonlauncher.models

import android.content.pm.LauncherApps
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.elnix90.logging.APP_LAUNCH_TAG
import io.github.elnix90.logging.logE
import io.github.elnix90.logging.logW
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Action
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
            val app: Application? = appRepository.findOne(launchAction.packageName, launchAction.profile.userHandle).first()
            if (app != null) {
                requestAppLaunch(app)
            }
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
            val activeProfiles = profileManager.activeProfiles.first()

            if (app.profile !in activeProfiles && isAtLeastApiLevel(28)) {
                profileManager.unlockProfile(app.profile)

                try {
                    withTimeoutOrNull(10_000L.milliseconds) {
                        profileManager.getProfileState(app.profile).first { it?.locked == false }
                    }?.let {
                        launchAppDirectly(app)
                    } ?: run {
                        logW(APP_LAUNCH_TAG) { "Timeout expired for profile unlock" }
                    }
                } catch (e: CancellationException) {
                    logE(APP_LAUNCH_TAG, e) { "App launch canceled" }
                }
            } else {
                launchAppDirectly(app)
            }
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
