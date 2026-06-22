package org.elnix.dragonlauncher.settings

import io.github.elnix90.core.stores.SettingsStore
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore

val themeSettingsStores: Set<SettingsStore<*, *>> by lazy {
    setOf(
        UiSettingsStore,
        ColorModesSettingsStore,
        ColorSettingsStore,
        AngleLineSettingsStore,
        HoldToActivateArcSettingsStore,
        DrawerSettingsStore
    )
}

