package org.elnix.dragonlauncher.models

import android.annotation.SuppressLint
import android.content.pm.LauncherApps
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.compat.PackageManagerCompat
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel
import org.elnix.dragonlauncher.logging.APP_LAUNCH_TAG
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.PermissionsManager
import org.elnix.dragonlauncher.profiles.ProfileManager
import org.elnix.dragonlauncher.recents.RecentsService
import org.elnix.dragonlauncher.settings.stores.map.WellbeingSettingsStore
import org.elnix.dragonlauncher.timer.AppTimerService
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class AppLaunchViewModel @Inject constructor(
    application: android.app.Application,
    private val recentsService: RecentsService,
    private val profileManager: ProfileManager,
    private val permissionsManager: PermissionsManager,
    private val packageManagerCompat: PackageManagerCompat,
    private val appRepository: AppRepository
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext


    val socialMediaPauseEnabled = WellbeingSettingsStore.socialMediaPauseEnabled.flow(ctx)
    val guiltModeEnabled = WellbeingSettingsStore.guiltModeEnabled.flow(ctx)
    val pauseDuration = WellbeingSettingsStore.pauseDurationSeconds.flow(ctx)
    val pausedApps = WellbeingSettingsStore.pausedApps.flow(ctx)
    val reminderEnabled = WellbeingSettingsStore.reminderEnabled.flow(ctx)
    val reminderInterval = WellbeingSettingsStore.reminderIntervalMinutes.flow(ctx)
    val reminderMode = WellbeingSettingsStore.reminderMode.flow(ctx)
    val returnToLauncherEnabled = WellbeingSettingsStore.returnToLauncherEnabled.flow(ctx)

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

    val hasUsageStatsPermission: StateFlow<Boolean> = permissionsManager.hasPermission(PermissionGroup.UsageStat).stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        false
    )

    private val _pendingAppLaunch = MutableStateFlow<Application?>(null)
    val pendingAppLaunch = _pendingAppLaunch.asStateFlow()

    private var currentLaunchJob: Job? = null


    fun requestAppLaunch(launchAction: Action.LaunchApp) {
        viewModelScope.launch {
            val app: Application? = appRepository.findOne(launchAction.packageName, launchAction.profile.userHandle).first()
            if (app != null) {
                requestAppLaunch(app)
            }
        }
    }

    fun requestAppLaunch(application: Application) {
        viewModelScope.launch{
            val startAppTimer = combine(pausedApps, socialMediaPauseEnabled) { pausedApps, socialMediaPauseEnabled ->
                if (!socialMediaPauseEnabled) return@combine false
                application.packageName in pausedApps
            }

            if(startAppTimer.first()) {
                _pendingAppLaunch.value = application
                return@launch
            }

            launchAppWithProfileUnlock(application)
        }
    }


    suspend fun startTimer(timeLimitMinutes: Int?, app: Application) {
        AppTimerService.start(
            ctx = ctx,
            application = app,
            reminderEnabled = reminderEnabled.first(),
            reminderIntervalMinutes = reminderInterval.first(),
            reminderMode = reminderMode.first(),
            timeLimitMinutes = timeLimitMinutes
        )
    }

    fun onAppTimerServiceStarted(duration: Int?): Boolean {
        val pendingApp = _pendingAppLaunch.value
        if (pendingApp!= null) {

            if (duration != null) {
                viewModelScope.launch{
                    startTimer(duration, pendingApp)
                }
            }

            launchAppDirectly(pendingApp)
            return true
        }
        return false
    }

    fun launchShortcut(action: Action.LaunchShortcut) {
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
                    withTimeoutOrNull(10_000L) {
                        profileManager.getProfileState(application.profile)
                            .filter { it?.locked == false }
                            .first()
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
            ?: throw AppLaunchException("LauncherApps unavailable")

        val packageName = application.packageName

        val activity = launcherApps
            .getActivityList(null, application.profile.userHandle)
            .firstOrNull { it.applicationInfo.packageName == packageName }
            ?: throw AppLaunchException("Launcher activity not found for $packageName")

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
            throw AppLaunchException("Security error launching $packageName", e)
        } catch (e: NullPointerException) {
            logE(APP_LAUNCH_TAG, e) { "App component not found for $packageName" }
            throw AppLaunchException("App component not found for $packageName", e)
        } catch (e: Exception) {
            logE(APP_LAUNCH_TAG, e) { "Failed to launch $packageName" }
            throw AppLaunchException("Failed to launch $packageName", e)
        }
    }


    init {
        viewModelScope.launch {
        }

        viewModelInitialized()
    }
}


/**
 * Exception for app launch failures
 */
class AppLaunchException(message: String, cause: Throwable? = null) : Exception(message, cause)
