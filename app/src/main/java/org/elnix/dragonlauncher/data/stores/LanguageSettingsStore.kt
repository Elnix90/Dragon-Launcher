package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.languageDatastore


object LanguageSettingsStore {
    private val KEY_LANG = stringPreferencesKey("pref_app_language")


    suspend fun setLanguageTag(ctx: Context, tag: String?) {
        ctx.languageDatastore.edit { prefs ->
            if (tag == null) prefs.remove(KEY_LANG) else prefs[KEY_LANG] = tag
        }
    }

    suspend fun getLanguageTag(ctx: Context): String? {
        return ctx.languageDatastore.data.map { it[KEY_LANG] }.first()
    }

    suspend fun resetAll(ctx: Context) {
        ctx.languageDatastore.edit { prefs ->
            prefs.remove(KEY_LANG)
        }
    }

    suspend fun getAll(ctx: Context): Map<String, String> {
        val prefs = ctx.languageDatastore.data.first()
        return buildMap {
            prefs[KEY_LANG]?.let { put(KEY_LANG.name, it) }
        }
    }

    suspend fun setAll(ctx: Context, data: Map<String, String>) {
        ctx.languageDatastore.edit { prefs ->
            data[KEY_LANG.name]?.let { prefs[KEY_LANG] = it }
        }
    }
}
