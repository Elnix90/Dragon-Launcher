package org.elnix.dragonlauncher.data.stores

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.getBooleanStrict
import org.elnix.dragonlauncher.data.getFloatStrict
import org.elnix.dragonlauncher.data.getStringStrict
import org.elnix.dragonlauncher.data.putIfNonDefault
import org.elnix.dragonlauncher.data.stores.WallpaperSettingsStore.Keys.DRAWER_BLURRED
import org.elnix.dragonlauncher.data.stores.WallpaperSettingsStore.Keys.DRAWER_BLUR_RADIUS
import org.elnix.dragonlauncher.data.stores.WallpaperSettingsStore.Keys.DRAWER_ORIGINAL
import org.elnix.dragonlauncher.data.stores.WallpaperSettingsStore.Keys.MAIN_BLURRED
import org.elnix.dragonlauncher.data.stores.WallpaperSettingsStore.Keys.MAIN_BLUR_RADIUS
import org.elnix.dragonlauncher.data.stores.WallpaperSettingsStore.Keys.MAIN_ORIGINAL
import org.elnix.dragonlauncher.data.stores.WallpaperSettingsStore.Keys.USE_ON_DRAWER
import org.elnix.dragonlauncher.data.stores.WallpaperSettingsStore.Keys.USE_ON_MAIN
import org.elnix.dragonlauncher.data.wallpaperSettingsStore
import java.io.ByteArrayOutputStream

object WallpaperSettingsStore : BaseSettingsStore<Map<String, Any?>>() {

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
            it[USE_ON_MAIN] = enabled
        }
    }

    fun getUseOnMain(ctx: Context): Flow<Boolean> =
        ctx.wallpaperSettingsStore.data.map {
            it[USE_ON_MAIN] ?: defaults.useOnMain
        }

    suspend fun setUseOnDrawer(ctx: Context, enabled: Boolean) {
        ctx.wallpaperSettingsStore.edit {
            it[USE_ON_DRAWER] = enabled
        }
    }

    fun getUseOnDrawer(ctx: Context): Flow<Boolean> =
        ctx.wallpaperSettingsStore.data.map {
            it[USE_ON_DRAWER] ?: defaults.useOnDrawer
        }

    suspend fun setMainBlurRadius(ctx: Context, radius: Float) {
        ctx.wallpaperSettingsStore.edit { it[MAIN_BLUR_RADIUS] = radius }
    }

    fun getMainBlurRadius(ctx: Context): Flow<Float> =
        ctx.wallpaperSettingsStore.data.map { it[MAIN_BLUR_RADIUS] ?: 0f }

    suspend fun setDrawerBlurRadius(ctx: Context, radius: Float) {
        ctx.wallpaperSettingsStore.edit { it[DRAWER_BLUR_RADIUS] = radius }
    }

    fun getDrawerBlurRadius(ctx: Context): Flow<Float> =
        ctx.wallpaperSettingsStore.data.map { it[DRAWER_BLUR_RADIUS] ?: 0f }


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
            it[MAIN_ORIGINAL] = bitmap.toBase64()
        }
    }

    suspend fun saveMainBlurred(
        ctx: Context,
        bitmap: Bitmap
    ) {
        ctx.wallpaperSettingsStore.edit {
            it[MAIN_BLURRED] = bitmap.toBase64()
        }
    }

    suspend fun saveDrawerOriginal(
        ctx: Context,
        bitmap: Bitmap
    ) {
        ctx.wallpaperSettingsStore.edit {
            it[DRAWER_ORIGINAL] = bitmap.toBase64()
        }
    }

    suspend fun saveDrawerBlurred(
        ctx: Context,
        bitmap: Bitmap
    ) {
        ctx.wallpaperSettingsStore.edit {
            it[DRAWER_BLURRED] = bitmap.toBase64()
        }
    }


    suspend fun loadMainOriginal(ctx: Context): Bitmap? =
        decodeBitmap(
            ctx.wallpaperSettingsStore.data
                .map { it[MAIN_ORIGINAL] }
                .firstOrNull()
        )

    suspend fun loadMainBlurred(ctx: Context): Bitmap? =
        decodeBitmap(
            ctx.wallpaperSettingsStore.data
                .map { it[MAIN_BLURRED] }
                .firstOrNull()
        )

    suspend fun loadDrawerOriginal(ctx: Context): Bitmap? =
        decodeBitmap(
            ctx.wallpaperSettingsStore.data
                .map { it[DRAWER_ORIGINAL] }
                .firstOrNull()
        )

    suspend fun loadDrawerBlurred(ctx: Context): Bitmap? =
        decodeBitmap(
            ctx.wallpaperSettingsStore.data
                .map { it[DRAWER_BLURRED] }
                .firstOrNull()
        )

    fun loadMainBlurredFlow(ctx: Context): Flow<Bitmap?> =
        ctx.wallpaperSettingsStore.data.map {
            decodeBitmap(it[MAIN_BLURRED])
        }

    fun loadDrawerBlurredFlow(ctx: Context): Flow<Bitmap?> =
        ctx.wallpaperSettingsStore.data.map {
            decodeBitmap(it[DRAWER_BLURRED])
        }


    override suspend fun resetAll(ctx: Context) {
        ctx.wallpaperSettingsStore.edit { prefs ->
            Keys.ALL.forEach { prefs.remove(it) }
        }
    }

    override suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.wallpaperSettingsStore.data.first()

        return buildMap {

            putIfNonDefault(
                USE_ON_MAIN,
                prefs[USE_ON_MAIN],
                defaults.useOnMain
            )

            putIfNonDefault(
                USE_ON_DRAWER,
                prefs[USE_ON_DRAWER],
                defaults.useOnDrawer
            )

            putIfNonDefault(
                MAIN_BLUR_RADIUS,
                prefs[MAIN_BLUR_RADIUS],
                defaults.mainBlurRadius
            )

            putIfNonDefault(
                DRAWER_BLUR_RADIUS,
                prefs[DRAWER_BLUR_RADIUS],
                defaults.drawerBlurRadius
            )

            putIfNonDefault(
                MAIN_ORIGINAL,
                prefs[MAIN_ORIGINAL],
                defaults.mainOriginal
            )

            putIfNonDefault(
                MAIN_BLURRED,
                prefs[MAIN_BLURRED],
                defaults.mainBlurred
            )

            putIfNonDefault(
                DRAWER_ORIGINAL,
                prefs[DRAWER_ORIGINAL],
                defaults.drawerOriginal
            )

            putIfNonDefault(
                DRAWER_BLURRED,
                prefs[DRAWER_BLURRED],
                defaults.drawerBlurred
            )
        }
    }



    override suspend fun setAll(ctx: Context, value: Map<String, Any?>) {
        ctx.wallpaperSettingsStore.edit { prefs ->
            prefs[USE_ON_MAIN] =
                getBooleanStrict(value, USE_ON_MAIN, defaults.useOnMain)

            prefs[USE_ON_DRAWER] =
                getBooleanStrict(value, USE_ON_DRAWER, defaults.useOnDrawer)

            prefs[MAIN_BLUR_RADIUS] =
                getFloatStrict(value, MAIN_BLUR_RADIUS, defaults.mainBlurRadius)

            prefs[DRAWER_BLUR_RADIUS] =
                getFloatStrict(value, DRAWER_BLUR_RADIUS, defaults.drawerBlurRadius)

            prefs[MAIN_ORIGINAL] =
                getStringStrict(value, MAIN_ORIGINAL, "")

            prefs[MAIN_BLURRED] =
                getStringStrict(value, MAIN_BLURRED, "")

            prefs[DRAWER_ORIGINAL] =
                getStringStrict(value, DRAWER_ORIGINAL, "")

            prefs[DRAWER_BLURRED] =
                getStringStrict(value, DRAWER_BLURRED, "")
        }
    }
}
