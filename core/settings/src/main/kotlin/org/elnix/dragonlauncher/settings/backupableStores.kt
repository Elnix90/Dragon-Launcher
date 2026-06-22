package org.elnix.dragonlauncher.settings

import io.github.elnix90.core.stores.SettingsStore

val backupableStores: Set<SettingsStore<*,*>> = AllStores.filter { it.backupable }.toSet()