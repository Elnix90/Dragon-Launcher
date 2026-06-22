package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.objects.stringSet
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object BackupSettingsStore : MapSettingsStore() {

    @SettingKey
    val autoBackupEnabled = boolean(
        title = R.string.automatic_backups,
        description = R.string.auto_backup_desc,
        default = false
    )

    @SettingKey
    val autoBackupUri = string(
        default = ""
    )


    /**
     * I use an empty set because it causes failures in runtime during the resolution of AllsStores for some reason
     */
    @SettingKey
    val backupStores = stringSet(
        title = R.string.auto_backup_stores,
        default = emptySet()
    )


    // TODO ( after  3.0.0 ) - Bruh
//    @SettingKey
//    val numberOfBackupsToKeep = int(
//        default = 2,
//////        allowedRange = 1..10
//    )
}