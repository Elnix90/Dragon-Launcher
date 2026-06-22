package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore

@SettingsStore
object LanguageSettingsStore : MapSettingsStore() {

    @SettingKey
    val keyLang = string("")
}