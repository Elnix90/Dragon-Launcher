package org.elnix.dragonlauncher.data.stores

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.wallpaperSettingsStore
import java.io.ByteArrayOutputStream

object WallpaperSettingsStore : BaseSettingsStore() {

    override val name: String = "Wallpaper"

    private data class Defaults(
        val useOnMain: Boolean = false,
        val useOnDrawer: Boolean = false,
        val mainBlurRadius: Float = 0f,
        val drawerBlurRadius: Float = 0f,
        val mainOriginal: String? = null,
        val mainBlurred: String? = null,
        val drawerOriginal: String? = null,
        val drawerBlurred: String? = null
    )

    private val defaults = Defaults()

    private object Keys {

        val USE_ON_MAIN =
            booleanPreferencesKey("wallpaper_use_on_main")

        val USE_ON_DRAWER =
            booleanPreferencesKey("wallpaper_use_on_drawer")

        val MAIN_BLUR_RADIUS =
            floatPreferencesKey("wallpaper_main_blur_radius")

        val DRAWER_BLUR_RADIUS =
            floatPreferencesKey("wallpaper_drawer_blur_radius")

        val MAIN_ORIGINAL =
            stringPreferencesKey("wallpaper_main_original_b64")

        val MAIN_BLURRED =
            stringPreferencesKey("wallpaper_main_blurred_b64")

        val DRAWER_ORIGINAL =
            stringPreferencesKey("wallpaper_drawer_original_b64")

        val DRAWER_BLURRED =
            stringPreferencesKey("wallpaper_drawer_blurred_b64")

        val ALL = listOf(
            USE_ON_MAIN,
            USE_ON_DRAWER,
            MAIN_BLUR_RADIUS,
            DRAWER_BLUR_RADIUS,
            MAIN_ORIGINAL,
            MAIN_BLURRED,
            DRAWER_ORIGINAL,
            DRAWER_BLURRED
        )
    }


    suspend fun setUseOnMain(ctx: Context, enabled: Boolean) {
        ctx.wallpaperSettingsStore.edit {
            it[Keys.USE_ON_MAIN] = enabled
        }
    }

    fun getUseOnMain(ctx: Context): Flow<Boolean> =
        ctx.wallpaperSettingsStore.data.map {
            it[Keys.USE_ON_MAIN] ?: defaults.useOnMain
        }

    suspend fun setUseOnDrawer(ctx: Context, enabled: Boolean) {
        ctx.wallpaperSettingsStore.edit {
            it[Keys.USE_ON_DRAWER] = enabled
        }
    }

    fun getUseOnDrawer(ctx: Context): Flow<Boolean> =
        ctx.wallpaperSettingsStore.data.map {
            it[Keys.USE_ON_DRAWER] ?: defaults.useOnDrawer
        }

    suspend fun setMainBlurRadius(ctx: Context, radius: Float) {
        ctx.wallpaperSettingsStore.edit { it[Keys.MAIN_BLUR_RADIUS] = radius }
    }

    fun getMainBlurRadius(ctx: Context): Flow<Float> =
        ctx.wallpaperSettingsStore.data.map { it[Keys.MAIN_BLUR_RADIUS] ?: 0f }

    suspend fun setDrawerBlurRadius(ctx: Context, radius: Float) {
        ctx.wallpaperSettingsStore.edit { it[Keys.DRAWER_BLUR_RADIUS] = radius }
    }

    fun getDrawerBlurRadius(ctx: Context): Flow<Float> =
        ctx.wallpaperSettingsStore.data.map { it[Keys.DRAWER_BLUR_RADIUS] ?: 0f }


    private fun Bitmap.toBase64(): String {
        val out = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, out)
        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
    }

    private fun decodeBitmap(b64: String?): Bitmap? {
        if (b64 == null) return null
        return try {
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveMainOriginal(
        ctx: Context,
        bitmap: Bitmap
    ) {
        ctx.wallpaperSettingsStore.edit {
            it[Keys.MAIN_ORIGINAL] = bitmap.toBase64()
        }
    }

    suspend fun saveMainBlurred(
        ctx: Context,
        bitmap: Bitmap
    ) {
        ctx.wallpaperSettingsStore.edit {
            it[Keys.MAIN_BLURRED] = bitmap.toBase64()
        }
    }

    suspend fun saveDrawerOriginal(
        ctx: Context,
        bitmap: Bitmap
    ) {
        ctx.wallpaperSettingsStore.edit {
            it[Keys.DRAWER_ORIGINAL] = bitmap.toBase64()
        }
    }

    suspend fun saveDrawerBlurred(
        ctx: Context,
        bitmap: Bitmap
    ) {
        ctx.wallpaperSettingsStore.edit {
            it[Keys.DRAWER_BLURRED] = bitmap.toBase64()
        }
    }


    suspend fun loadMainOriginal(ctx: Context): Bitmap? =
        decodeBitmap(
            ctx.wallpaperSettingsStore.data
                .map { it[Keys.MAIN_ORIGINAL] }
                .firstOrNull()
        )

    suspend fun loadMainBlurred(ctx: Context): Bitmap? =
        decodeBitmap(
            ctx.wallpaperSettingsStore.data
                .map { it[Keys.MAIN_BLURRED] }
                .firstOrNull()
        )

    suspend fun loadDrawerOriginal(ctx: Context): Bitmap? =
        decodeBitmap(
            ctx.wallpaperSettingsStore.data
                .map { it[Keys.DRAWER_ORIGINAL] }
                .firstOrNull()
        )

    suspend fun loadDrawerBlurred(ctx: Context): Bitmap? =
        decodeBitmap(
            ctx.wallpaperSettingsStore.data
                .map { it[Keys.DRAWER_BLURRED] }
                .firstOrNull()
        )

    fun loadMainOriginalFlow(ctx: Context): Flow<Bitmap?> =
        ctx.wallpaperSettingsStore.data.map {
            decodeBitmap(it[Keys.MAIN_ORIGINAL])
        }

    fun loadMainBlurredFlow(ctx: Context): Flow<Bitmap?> =
        ctx.wallpaperSettingsStore.data.map {
            decodeBitmap(it[Keys.MAIN_BLURRED])
        }

    fun loadDrawerOriginalFlow(ctx: Context): Flow<Bitmap?> =
        ctx.wallpaperSettingsStore.data.map {
            decodeBitmap(it[Keys.DRAWER_ORIGINAL])
        }

    fun loadDrawerBlurredFlow(ctx: Context): Flow<Bitmap?> =
        ctx.wallpaperSettingsStore.data.map {
            decodeBitmap(it[Keys.DRAWER_BLURRED])
        }


    override suspend fun resetAll(ctx: Context) {
        ctx.wallpaperSettingsStore.edit { prefs ->
            Keys.ALL.forEach { prefs.remove(it) }
        }
    }

    suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.wallpaperSettingsStore.data.first()

        return buildMap {

            fun putIfNotDefault(
                key: Preferences.Key<Boolean>,
                default: Boolean
            ) {
                val v = prefs[key] ?: return
                if (v != default) put(key.name, v)
            }

            fun putIfNotDefault(
                key: Preferences.Key<Float>,
                default: Float
            ) {
                val v = prefs[key] ?: return
                if (v != default) put(key.name, v)
            }

            fun putIfNotDefault(
                key: Preferences.Key<String>,
                default: String?
            ) {
                val v = prefs[key] ?: return
                if (v != default) put(key.name, v)
            }

            putIfNotDefault(Keys.USE_ON_MAIN, defaults.useOnMain)
            putIfNotDefault(Keys.USE_ON_DRAWER, defaults.useOnDrawer)

            putIfNotDefault(Keys.MAIN_BLUR_RADIUS, defaults.mainBlurRadius)
            putIfNotDefault(Keys.DRAWER_BLUR_RADIUS, defaults.drawerBlurRadius)

            putIfNotDefault(Keys.MAIN_ORIGINAL, defaults.mainOriginal)
            putIfNotDefault(Keys.MAIN_BLURRED, defaults.mainBlurred)
            putIfNotDefault(Keys.DRAWER_ORIGINAL, defaults.drawerOriginal)
            putIfNotDefault(Keys.DRAWER_BLURRED, defaults.drawerBlurred)
        }
    }



    suspend fun setAll(ctx: Context, backup: Map<String, Any?>) {
        ctx.wallpaperSettingsStore.edit { prefs ->

            fun apply(key: Preferences.Key<Boolean>) {
                (backup[key.name] as? Boolean)?.let { prefs[key] = it }
            }

            fun apply(key: Preferences.Key<Float>) {
                (backup[key.name] as? Number)?.toFloat()?.let {
                    prefs[key] = it
                }
            }

            fun apply(key: Preferences.Key<String>) {
                (backup[key.name] as? String)?.let {
                    prefs[key] = it
                }
            }

            apply(Keys.USE_ON_MAIN)
            apply(Keys.USE_ON_DRAWER)
            apply(Keys.MAIN_BLUR_RADIUS)
            apply(Keys.DRAWER_BLUR_RADIUS)
            apply(Keys.MAIN_ORIGINAL)
            apply(Keys.MAIN_BLURRED)
            apply(Keys.DRAWER_ORIGINAL)
            apply(Keys.DRAWER_BLURRED)
        }
    }
}
