package org.elnix.dragonlauncher.recents

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.model.models.Application
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

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        scope.launch {
            loadRecentlyUsedApps()
        }
    }

    private suspend fun loadRecentlyUsedApps() {
        val json = DrawerSettingsStore.recentlyUsedPackages.get(ctx)
        if (json.isNotEmpty()) {
            try {
                _recentlyUsedPackages.value = json.toList()
            } catch (_: Exception) {
                _recentlyUsedPackages.value = emptyList()
            }
        }
    }

    /**
     * Record a package as recently used.
     * Moves it to the front if already present, trims the list to a reasonable max.
     */
    override fun touch(application: Application) {
        val packageName = application.packageName
        val maxStored = 30 // store more than display, user can raise the count later
        val current = _recentlyUsedPackages.value.toMutableList()
        current.remove(packageName)
        current.add(0, packageName)
        val trimmed = current.take(maxStored)
        _recentlyUsedPackages.value = trimmed
        scope.launch {
            DrawerSettingsStore.recentlyUsedPackages.set(ctx, trimmed.toSet())
        }
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