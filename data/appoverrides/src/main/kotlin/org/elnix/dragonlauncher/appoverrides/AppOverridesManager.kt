@file:Suppress("unused")

package org.elnix.dragonlauncher.appoverrides

import android.content.Context
import io.github.elnix90.logging.WORKSPACES_TAG
import io.github.elnix90.logging.logE
import io.github.elnix90.logging.logWtf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.AppOverride
import org.elnix.dragonlauncher.base.model.serializables.AppOverride.Companion.defaultAppOverrides
import org.elnix.dragonlauncher.base.model.serializables.AppOverrideState
import org.elnix.dragonlauncher.base.model.serializables.CacheKey
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon
import org.elnix.dragonlauncher.settings.stores.array.AppOverridesSettingsStore


public object AppOverridesJson : DragonJson<AppOverrideState>()


public class AppOverridesManager(
    private val ctx: Context
) {

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    public val appOverrides: SettingFlow<AppOverrideState> = SettingFlow(defaultAppOverrides)

    init {
        scope.launch { loadAppOverrides() }
    }

    private suspend fun loadAppOverrides() = withContext(Dispatchers.IO) {
        try {
            val jsonString = AppOverridesSettingsStore.jsonSetting.get(ctx)
            if (jsonString.isBlank()) return@withContext

            val loadedState = AppOverridesJson.decode(jsonString, defaultAppOverrides)
            appOverrides.value = loadedState

        } catch (e: Exception) {
            logE(WORKSPACES_TAG, e) { "Error while loading the overrides state" }
            appOverrides.value = defaultAppOverrides
        }
    }

    private fun persistAppOverrides() = scope.launch(Dispatchers.IO) {
        if (appOverrides.value == defaultAppOverrides) return@launch
        val json = AppOverridesJson.encode(appOverrides.value)
        AppOverridesSettingsStore.jsonSetting.set(ctx, json)
    }


    private inline fun update(newAppOverrides: (AppOverrideState) -> AppOverrideState) {
        appOverrides.value = newAppOverrides(appOverrides.value)
        persistAppOverrides()
    }

    private inline fun updateOv(cacheKey: CacheKey, newOverride: (AppOverride) -> AppOverride?) {
        update { old ->
            val prevOverride = old[cacheKey] ?: AppOverride()
            val newOverride = newOverride(prevOverride)

            val new = if (newOverride != null && newOverride.isNotNullOrEmpty) {
                logWtf { "New isn't empty: $newOverride" }
                old + (cacheKey to newOverride)
            } else {
                logWtf { "New is empty: $newOverride" }
                old - cacheKey
            }

            logWtf { "Updating app overrides:\nold : $old\nnew : $new\n " }
            new
        }
    }

    public fun getAliasesForApp(app: Application): Flow<Set<String>> {
        return appOverrides.flow.map { it[app.key]?.aliases ?: emptySet() }
    }

    public fun addAliasToApp(alias: String, cacheKey: CacheKey) {
        updateOv(cacheKey) { old ->
            old.copy(aliases = (old.aliases ?: emptySet()).plus(alias))
        }
    }

    public fun updateAliasToApp(old: String, new: String, cacheKey: CacheKey) {
        updateOv(cacheKey) { override ->
            val currentAliases = override.aliases ?: return
            val newAliases = currentAliases.mapTo(mutableSetOf()) {
                if (it == old) new
                else it
            }
            override.copy(aliases = newAliases.takeIf { it.isNotEmpty() })
        }
    }

    public fun removeAliasFromApp(cacheKey: CacheKey, aliasToRemove: String) {
        updateOv(cacheKey) { old ->
            val newAliases = old.aliases?.minus(aliasToRemove)?.takeIf { it.isNotEmpty() }
            old.copy(aliases = newAliases)
        }
    }

    public fun resetAliasForApp(cacheKey: CacheKey) {
        updateOv(cacheKey) { old ->
            old.copy(aliases = null)
        }
    }

    public fun renameApp(cacheKey: CacheKey, customName: String?) {
        updateOv(cacheKey) { old ->
            old.copy(customName = customName?.takeIf { it.isNotEmpty() })
        }
    }

    public fun setAppIcon(cacheKey: CacheKey, customIcon: CustomIcon?) {
        updateOv(cacheKey) { old ->
            old.copy(customIcon = customIcon)
        }
    }

//    /**
//     * Mainly debug funny thing, it's like customizing all app icons at once
//     * for each app installed, it applies to it the custom icon
//     *
//     * @param icon
//     */
//    fun applyIconToApps(
//        icon: CustomIcon?
//    ) {
//        scope.launch {
//            iconSemaphore.withPermit {
//
//                // Store icon ONCE
//                val sharedIcon = icon?.copy()
//
//                _workspacesState.value = _workspacesState.value.copy(
//                    appOverrides = _apps.value.associate {
//                        (it.key to AppOverride(customIcon = sharedIcon))
//                    }
//                )
//            }
//        }
//        persistWorkspaces()
//    }


    public fun resetOverrides() {
        appOverrides.value = defaultAppOverrides

        scope.launch {
            AppOverridesSettingsStore.resetAll(ctx)
        }
    }
}