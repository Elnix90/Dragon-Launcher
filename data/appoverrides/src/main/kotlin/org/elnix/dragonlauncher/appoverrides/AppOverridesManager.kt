@file:Suppress("unused")

package org.elnix.dragonlauncher.appoverrides

import android.content.Context
import io.github.elnix90.logging.WORKSPACES_TAG
import io.github.elnix90.logging.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.AppOverride
import org.elnix.dragonlauncher.base.model.serializables.AppOverride.Companion.defaultAppOverrides
import org.elnix.dragonlauncher.base.model.serializables.AppOverride.Companion.isNotNullOrEmpty
import org.elnix.dragonlauncher.base.model.serializables.AppOverrideState
import org.elnix.dragonlauncher.base.model.serializables.CacheKey
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon
import org.elnix.dragonlauncher.settings.stores.array.AppOverridesSettingsStore


public object AppOverridesJson : DragonJson<AppOverrideState>()


public class AppOverridesManager(
    private val ctx: Context
) {

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    public val appOverridesState: SettingFlow<AppOverrideState> = SettingFlow(defaultAppOverrides)

    init {
        scope.launch { loadAppOverrides() }
    }

    private suspend fun loadAppOverrides() = withContext(Dispatchers.IO) {
        try {
            val jsonString = AppOverridesSettingsStore.jsonSetting.get(ctx)
            if (jsonString.isBlank()) return@withContext

            val loadedState = AppOverridesJson.decode(jsonString, defaultAppOverrides)
            appOverridesState.value = loadedState

        } catch (e: Exception) {
            logE(WORKSPACES_TAG, e) { "Error while loading the overrides state" }
            appOverridesState.value = defaultAppOverrides
        }
    }

    private fun persistAppOverrides() = scope.launch(Dispatchers.IO) {
        if (appOverridesState.value == defaultAppOverrides) return@launch
        val json = AppOverridesJson.encode(appOverridesState.value)
        AppOverridesSettingsStore.jsonSetting.set(ctx, json)
    }


    private inline fun update(newAppOverridesState: (AppOverrideState) -> AppOverrideState?) {
        newAppOverridesState(appOverridesState.value)?.let {
            appOverridesState.value = it
            persistAppOverrides()
        }
    }

    private inline fun updateOv(cacheKey: CacheKey, newOverride: (AppOverride) -> AppOverride?) {
        update { old ->
            val prevOverride = old[cacheKey] ?: AppOverride()
            val newOverride = newOverride(prevOverride)

            if (newOverride.isNotNullOrEmpty) {
                old + (cacheKey to newOverride)
            } else {
                old - cacheKey
            }

            old
        }
    }

    public fun getAliasesForApp(app: Application): List<String> {
        return appOverridesState.value[app.key]?.aliases ?: emptyList()
    }

    public fun addAliasToApp(alias: String, cacheKey: CacheKey) {
        updateOv(cacheKey) { old ->
            old.copy(aliases = (old.aliases ?: emptySet()).plus(alias))
        }
    }

    public fun updateAliasToApp(old: String, new: String, cacheKey: CacheKey) {
        updateOv(cacheKey) { override ->
            val currentAliases = override.aliases ?: emptyList()
            val updatedAliases = currentAliases.toMutableList().apply {
                val oldIndex = indexOf(old)
                if (oldIndex != -1) {
                    set(oldIndex, new)
                } else {
                    add(new)
                }
            }
            override.copy(aliases = updatedAliases)
        }
    }

    public fun removeAliasFromApp(cacheKey: CacheKey, aliasToRemove: String) {
        updateOv(cacheKey) { old ->
            old.copy(aliases = old.aliases?.minus(aliasToRemove)?.takeIf { it.isNotEmpty() })
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
        appOverridesState.value = defaultAppOverrides

        scope.launch {
            AppOverridesSettingsStore.resetAll(ctx)
        }
    }
}