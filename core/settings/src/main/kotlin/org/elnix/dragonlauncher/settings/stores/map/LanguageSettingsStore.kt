package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.StringSettingObject
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore

@SettingsStore
public object LanguageSettingsStore : MapSettingsStore() {

    @SettingKey
    public val keyLang: StringSettingObject = string("")
}