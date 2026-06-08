package org.elnix.dragonlauncher.recents

import android.content.Context
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.logging.logWtf
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore


// TODO track correctly app launches in recent apps profile aware

interface RecentsService {

    fun touch(application: Application)
    fun getRecentApps(count: Int): StateFlow<List<Application>>
}


internal class RecentsServiceImpl(
    private val ctx: Context,
    private val applicationRepository: AppRepository
) : RecentsService {

    private val _recentlyUsedPackages = MutableStateFlow<List<String>>(emptyList())

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        logWtf(exception) { "COROUTINE CRASHED: ${exception.message}" }
    }

    private val scope = CoroutineScope(
        Dispatchers.IO + SupervisorJob() + exceptionHandler
    )
//    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        init {
            logWtf { "RecentsServiceImpl companion object loaded" }
        }
    }

    init {
        logWtf { "RecentsServiceImpl.init() called" }
        scope.launch {
            logWtf { "Coroutine launched" }
            loadRecentlyUsedApps()
            logWtf { "loadRecentlyUsedApps returned" }
        }
    }

    private suspend fun loadRecentlyUsedApps() {
        return withContext(Dispatchers.IO) {
            try {
                logWtf { "Getting recent apps json" }

                logWtf { "DrawerSettingsStore = $DrawerSettingsStore" }
                logWtf { "recentlyUsedAppsCount = ${DrawerSettingsStore.recentlyUsedAppsCount}" }
                logWtf { "About to call getOrNull()" }


                val json = DrawerSettingsStore.recentlyUsedAppsCount.get(ctx)

//                val json = DrawerSettingsStore.recentlyUsedPackages.get(ctx)
                logWtf { "Json recent apps: $json" }
//                if (json.isNotEmpty()) {
//                    try {
//                        _recentlyUsedPackages.value = json.toList()
//                    } catch (_: Exception) {
//                        _recentlyUsedPackages.value = emptyList()
//                    }
//                }

            } catch (e: Exception) {
                logWtf(e) { "FATAL error in loadRecentlyUsedApps" }
                _recentlyUsedPackages.value = emptyList()
            }
        }
    }

    /**
     * Record a package as recently used.
     * Moves it to the front if already present, trims the list to a reasonable max.
     */
    override fun touch(application: Application) {
//        val packageName = application.packageName
//        val maxStored = 30 // store more than display, user can raise the count later
//        val current = _recentlyUsedPackages.value.toMutableList()
//        current.remove(packageName)
//        current.add(0, packageName)
//        val trimmed = current.take(maxStored)
//        _recentlyUsedPackages.value = trimmed
//        scope.launch {
//            DrawerSettingsStore.recentlyUsedPackages.set(ctx, trimmed.toSet())
//        }
    }

    /**
     * Returns the recently used [Application]s, resolved from the current app list.
     * Uses combine to reactively update when either apps or recent packages change.
     * @param count max number of recent apps to return
     */
    override fun getRecentApps(count: Int): StateFlow<List<Application>> {
        return _recentlyUsedPackages.combine(applicationRepository.getAllApps()) { packages, apps ->
            val allApps = apps.associateBy { it.packageName }
            packages
                .take(count)
                .mapNotNull { pkg -> allApps[pkg] }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())
    }
}