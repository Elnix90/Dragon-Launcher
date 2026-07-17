package org.elnix.dragonlauncher.models

import android.annotation.SuppressLint
import android.content.pm.LauncherApps
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
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

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext


//    data class WellbeingState(
//        val socialMediaPauseEnabled: Boolean,
//        val guiltModeEnabled: Boolean,
//        val pauseDuration: Int,
//        val pausedApps: Set<String>,
//        val reminderEnabled: Boolean,
//        val reminderInterval: Int,
//        val reminderMode: ReminderMode,
//        val returnToLauncherEnabled: Boolean
//    )

//
//    val wellbeingState: Flow<WellbeingState> = combineTransform(
//        socialMediaPauseEnabled,
//        guiltModeEnabled,
//        pauseDuration,
//        pausedApps,
//        reminderInterval,
//        reminderEnabled,
//        reminderMode,
//        returnToLauncherEnabled
//    ) { flows ->
//        @Suppress("UNCHECKED_CAST")
//        WellbeingState(
//            socialMediaPauseEnabled = flows[0] as Boolean,
//            guiltModeEnabled = flows[1] as Boolean,
//            pauseDuration = flows[2] as Int,
//            pausedApps = flows[3] as Set<String>,
//            reminderInterval = flows[4] as Int,
//            reminderEnabled = flows[5] as Boolean,
//            reminderMode = flows[6] as ReminderMode,
//            returnToLauncherEnabled = flows[7] as Boolean
//        )
//    }

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

    public fun requestAppLaunch(application: Application) {
        viewModelScope.launch {
            val startAppTimer =
                if (!WellbeingSettingsStore.socialMediaPauseEnabled.get(ctx)) false
                else application.packageName in WellbeingSettingsStore.pausedApps.get(ctx)

            if (startAppTimer) {
                pendingAppLaunch.value = application
                return@launch
            }

            launchAppWithProfileUnlock(application)
        }
    }


    public suspend fun startTimer(timeLimitMinutes: Int?, app: Application) {
        AppTimerService.start(
            ctx = ctx,
            application = app,
            reminderEnabled = WellbeingSettingsStore.reminderEnabled.flow(ctx).first(),
            reminderIntervalMinutes = WellbeingSettingsStore.reminderIntervalMinutes.flow(ctx).first(),
            reminderMode = WellbeingSettingsStore.reminderMode.flow(ctx).first().toString(),
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

    private fun launchAppWithProfileUnlock(application: Application) {

        currentLaunchJob?.cancel()

        currentLaunchJob = viewModelScope.launch {
            val activeProfiles = profileManager.activeProfiles.first()

            if (application.profile !in activeProfiles && isAtLeastApiLevel(28)) {
                profileManager.unlockProfile(application.profile)

                try {
                    withTimeoutOrNull(10_000L.milliseconds) {
                        profileManager.getProfileState(application.profile).first { it?.locked == false }
                    }?.let {
                        launchAppDirectly(application)
                    } ?: run {
                        logW(APP_LAUNCH_TAG) { "Timeout expired for profile unlock" }
                    }
                } catch (e: CancellationException) {
                    logE(APP_LAUNCH_TAG, e) { "App launch canceled" }
                }
            } else {
                launchAppDirectly(application)
            }
        }
    }


    /**
     * Launch an app directly without any pause check.
     * Used both by launchAction and after the digital pause screen.
     */
    private fun launchAppDirectly(application: Application) {
        val launcherApps = ctx.getSystemService(LauncherApps::class.java)

        val packageName = application.packageName

        val activity = launcherApps
            .getActivityList(null, application.profile.userHandle)
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
                application.profile.userHandle,
                null,
                options
            )

            recentsService.touch(application)

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
