package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.settings.SettingKey
import io.github.elnix90.settings.SettingStore
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

@SettingStore
object LanguageSettingsStore : MapSettingsStore(DataStoreName.LANGUAGE) {

    @SettingKey
    val keyLang = string(
        title = null,
        description = null,
        default = "",
    )
}