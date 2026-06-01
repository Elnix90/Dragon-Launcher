@file:Suppress("unused")

package org.elnix.dragonlauncher.appoverrides

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.AppOverride
import org.elnix.dragonlauncher.base.model.serializables.AppOverride.Companion.isNotNullOrEmpty
import org.elnix.dragonlauncher.base.model.serializables.AppOverrideState
import org.elnix.dragonlauncher.base.model.serializables.CacheKey
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon
import org.elnix.dragonlauncher.logging.WORKSPACES_TAG
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.settings.stores.`object`.AppOverridesSettingsStore


object AppOverridesJson : DragonJson<AppOverrideState>()
class AppOverridesManager(
    private val ctx: Context
) {

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private val _appOverridesState = MutableStateFlow(AppOverrideState())
    val appOverrideState = _appOverridesState.asStateFlow()



    init {
        scope.launch { loadAppOverrides() }
    }
    private suspend fun loadAppOverrides() = withContext(Dispatchers.IO) {
        try {
            val jsonString = AppOverridesSettingsStore.jsonSetting.get(ctx)
            if (jsonString.isBlank()) return@withContext

            val loadedState = AppOverridesJson.decode(jsonString) ?: AppOverrideState()
            _appOverridesState.value = loadedState

        } catch (e: Exception) {
            logE(WORKSPACES_TAG, e) { "Error while loading the overrides state" }
            _appOverridesState.value = AppOverrideState()
        }
    }

    private fun persistAppOverrides() = scope.launch(Dispatchers.IO) {
        val json = AppOverridesJson.encode(_appOverridesState.value)
        AppOverridesSettingsStore.jsonSetting.set(ctx, json)
    }


    private inline fun update(newAppOverridesState: (AppOverrideState) -> AppOverrideState?) {
        newAppOverridesState(_appOverridesState.value)?.let {
            _appOverridesState.value = it
            persistAppOverrides()
        }
    }

    private inline fun updateOv(cacheKey: CacheKey, newOverride: (AppOverride) -> AppOverride?) {
        update { old ->
            val prevOverrides = old.appOverrides
            val prevOverride = prevOverrides[cacheKey] ?: AppOverride()
            val newOverride = newOverride(prevOverride)

            old.copy(
                appOverrides = if (newOverride.isNotNullOrEmpty) {
                    prevOverrides + (cacheKey to newOverride)
                } else {
                    prevOverrides - cacheKey
                }
            )
        }
    }

    fun getAliasesForApp(app: Application): List<String> {
        return _appOverridesState.value.appOverrides[app.key]?.aliases ?: emptyList()
    }

    fun addAliasToApp(alias: String, cacheKey: CacheKey) {
        updateOv(cacheKey) { old ->
            old.copy(aliases = (old.aliases ?: emptySet()).plus(alias))
        }
    }

    fun updateAliasToApp(old: String, new: String, cacheKey: CacheKey) {
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

    fun removeAliasFromApp(cacheKey: CacheKey, aliasToRemove: String) {
        updateOv(cacheKey) { old ->
            old.copy(aliases = old.aliases?.minus(aliasToRemove)?.takeIf { it.isNotEmpty() })
        }
    }

    fun renameApp(cacheKey: CacheKey, customName: String?) {
        updateOv(cacheKey) { old ->
            old.copy(customName = customName?.takeIf { it.isNotEmpty() })
        }
    }

    fun setAppIcon(cacheKey: CacheKey, customIcon: CustomIcon?) {
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


    fun resetOverrides() {
        _appOverridesState.value = AppOverrideState()

        scope.launch {
            AppOverridesSettingsStore.resetAll(ctx)
        }
    }
}