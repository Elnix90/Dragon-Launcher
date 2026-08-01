package org.elnix.dragonlauncher

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.HiltAndroidApp
import io.github.elnix90.core.stores.JsonArraySettingsStore
import io.github.elnix90.core.stores.JsonObjectSettingsStore
import io.github.elnix90.core.stores.MapSettingsStore
import io.github.elnix90.logging.SETTINGS_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.elnix.dragonlauncher.settings.AllStores
import org.elnix.dragonlauncher.settings.stores.map.LanguageSettingsStore
import timber.log.Timber

@HiltAndroidApp
class DragonLauncherApplication : Application() {
    val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )


    @SuppressLint("LogNotTimber")
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("DragonCrash", "FATAL CRASH on thread ${thread.name}: ${throwable.message}", throwable)


            runBlocking {
                org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore.lastCrashStackTrace.set(
                    this@DragonLauncherApplication,
                    throwable.stackTraceToString()
                )
            }

            defaultHandler?.uncaughtException(thread, throwable)
        }

        Timber.plant(Timber.DebugTree())

        initializeAllStores()

        CoroutineScope(Dispatchers.Default).launch {
            val tag = LanguageSettingsStore.keyLang.get(this@DragonLauncherApplication)
            if (tag.isNotEmpty()) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(tag)
                )
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        appScope.cancel()
    }

    private fun initializeAllStores() {
        var totalSettings = 0
        var totalJsonObject = 0
        var totalJsonArray = 0

        AllStores.forEach { store ->

            when (store) {
                is JsonArraySettingsStore -> {
                    logI(SETTINGS_TAG) { "Initializing ${store.name} (jsonArray)" }
                    totalJsonArray++
                }

                is JsonObjectSettingsStore -> {
                    logI(SETTINGS_TAG) { "Initializing ${store.name} (jsonObject)" }
                    totalJsonObject++
                }

                is MapSettingsStore -> {
                    val settingsNumber = store.ALL.size
                    totalSettings += settingsNumber
                    logI(SETTINGS_TAG) { "Initializing ${store.name} ($settingsNumber settings)" }
                    store.ALL.forEach {
                        logD(SETTINGS_TAG) { "    - ${it.key}" }
                    }
                }
            }
        }

        logI(SETTINGS_TAG) { "Finished initializing settings;${AllStores.size} total stores, with: $totalSettings different settings and $totalJsonObject JsonObject and $totalJsonArray JsonArray stores" }
    }
}
