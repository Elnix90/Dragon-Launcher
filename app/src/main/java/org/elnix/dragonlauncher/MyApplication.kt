package org.elnix.dragonlauncher

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.data.stores.LanguageSettingsStore
import org.elnix.dragonlauncher.utils.AppDrawerViewModel

class MyApplication : Application() {
    val appViewModel by lazy {
        AppDrawerViewModel(this)
    }

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.Default).launch {
            val tag = LanguageSettingsStore.getLanguageTag(this@MyApplication)
            if (!tag.isNullOrEmpty()) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(tag)
                )
            }
        }
    }
}
