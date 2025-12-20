package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.appDrawerDataStore

object AppsSettingsStore : BaseSettingsStore() {
    override val name: String = "Apps"

    private val DATASTORE_KEY = stringPreferencesKey("cached_apps_json")

    suspend fun getCachedApps(ctx: Context): String? {
        return ctx.appDrawerDataStore.data
            .map { it[DATASTORE_KEY] }
            .firstOrNull()
    }

    suspend fun saveCachedApps(ctx: Context, json: String) {
        ctx.appDrawerDataStore.edit { prefs ->
            prefs[DATASTORE_KEY] = json
        }
    }

    override suspend fun resetAll(ctx: Context) {
        ctx.appDrawerDataStore.edit {
            it.remove(DATASTORE_KEY)
        }
    }
}
