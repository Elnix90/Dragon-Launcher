package org.elnix.dragonlauncher

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.elnix.dragonlauncher.logging.SETTINGS_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.settings.allStores
import org.elnix.dragonlauncher.settings.stores.map.LanguageSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore

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
                PrivateSettingsStore.lastCrashStackTrace.set(this@DragonLauncherApplication, throwable.stackTraceToString())
            }

            defaultHandler?.uncaughtException(thread, throwable)
        }

        CoroutineScope(Dispatchers.Default).launch {

            initializeAllStores()

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


    fun initializeAllStores() {
        allStores.forEach { (name, store) ->
            logD(SETTINGS_TAG) { "Initialized $name: $store"}
        }
    }
}
