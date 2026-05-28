package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.JsonArraySettingsStore

object PrivateAppsSettingsStore : JsonArraySettingsStore(DataStoreName.PRIVATE_APPS)
