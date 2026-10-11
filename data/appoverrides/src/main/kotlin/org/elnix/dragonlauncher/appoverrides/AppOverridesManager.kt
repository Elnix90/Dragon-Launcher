package org.elnix.dragonlauncher.appoverrides

import android.content.Context
import io.github.elnix90.logging.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.WORKSPACES_TAG
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.AppOverride
import org.elnix.dragonlauncher.base.model.serializables.AppOverride.Companion.defaultAppOverrides
import org.elnix.dragonlauncher.base.model.serializables.AppOverrideState
import org.elnix.dragonlauncher.base.model.serializables.CacheKey
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon
import org.elnix.dragonlauncher.base.model.serializables.CustomIconProperties
import org.elnix.dragonlauncher.settings.stores.objects.AppOverridesSettingsStore

object AppOverridesJson : DragonJson<AppOverrideState>()

class AppOverridesManager(
	private val ctx: Context
) {
	private val scope = CoroutineScope(Job() + Dispatchers.IO)

	val appOverrides: SettingFlow<AppOverrideState> = SettingFlow(defaultAppOverrides)

	init {
		scope.launch { loadAppOverrides() }
	}

	private suspend fun loadAppOverrides() =
		withContext(Dispatchers.IO) {
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

	private fun persistAppOverrides() =
		scope.launch(Dispatchers.IO) {
			if (appOverrides.value == defaultAppOverrides) return@launch
			val json = AppOverridesJson.encode(appOverrides.value)
			AppOverridesSettingsStore.jsonSetting.set(ctx, json)
		}

	private inline fun update(cacheKey: CacheKey, newOverride: (AppOverride) -> AppOverride?) {
		val prevOverride = appOverrides.value[cacheKey] ?: AppOverride()
		val newOverride = newOverride(prevOverride)

		appOverrides.value =
			if (newOverride != null && newOverride.isNotNullOrEmpty) {
				appOverrides.value + (cacheKey to newOverride)
			} else {
				appOverrides.value - cacheKey
			}

		persistAppOverrides()
	}

	fun getAliasesForApp(app: Application): Flow<Set<String>> = appOverrides.flow.map { it[app.key]?.aliases ?: emptySet() }

	fun addAliasToApp(alias: String, cacheKey: CacheKey) {
		update(cacheKey) { old ->
			old.copy(aliases = (old.aliases ?: emptySet()).plus(alias))
		}
	}

	fun updateAliasToApp(old: String, new: String, cacheKey: CacheKey) {
		update(cacheKey) { override ->
			val currentAliases = override.aliases ?: return
			val newAliases =
				currentAliases.mapTo(mutableSetOf()) {
					if (it == old) {
						new
					} else {
						it
					}
				}
			override.copy(aliases = newAliases.takeIf { it.isNotEmpty() })
		}
	}

	fun removeAliasFromApp(cacheKey: CacheKey, aliasToRemove: String) {
		update(cacheKey) { old ->
			val newAliases = old.aliases?.minus(aliasToRemove)?.takeIf { it.isNotEmpty() }
			old.copy(aliases = newAliases)
		}
	}

	fun resetAliasForApp(cacheKey: CacheKey) {
		update(cacheKey) { old ->
			old.copy(aliases = null)
		}
	}

	fun renameApp(cacheKey: CacheKey, customName: String?) {
		update(cacheKey) { old ->
			old.copy(customName = customName?.takeIf { it.isNotEmpty() })
		}
	}

	fun setAppCustomization(
		cacheKey: CacheKey,
		customIcon: CustomIcon?,
		iconProperties: CustomIconProperties?
	) {
		update(cacheKey) { old ->
			old.copy(
				customIcon = customIcon,
				iconProperties = iconProperties
			)
		}
	}

	fun setCustomCategory(cacheKey: CacheKey, categoryName: String?) {
		update(cacheKey) { old ->
			old.copy(customCategory = categoryName?.takeIf { it.isNotEmpty() })
		}
	}

	fun resetOverrides() {
		appOverrides.value = defaultAppOverrides

		scope.launch {
			AppOverridesSettingsStore.resetAll(ctx)
		}
	}
}
