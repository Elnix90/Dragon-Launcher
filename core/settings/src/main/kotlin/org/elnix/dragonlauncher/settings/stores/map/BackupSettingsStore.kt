package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.settings.SettingKey
import io.github.elnix90.settings.SettingStore
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.StringSetSettingObject.Companion.stringSet
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

@SettingStore
object BackupSettingsStore : MapSettingsStore(DataStoreName.BACKUP) {

    @SettingKey
    val autoBackupEnabled = boolean(
        title = R.string.automatic_backups,
        description = R.string.auto_backup_desc,
        default = false
    )

    @SettingKey
    val autoBackupUri = string(
        title = null,
        description = null,
        default = ""
    )

    /**
     * Because it caused crash at runtime due to early .entries initialization
     */
    private val defaultBackupStores: Set<String>
        get() = DataStoreName.entries
            .filter { it.userBackup }
            .map { it.value }
            .toSet()

    @SettingKey
    val backupStores = stringSet(
        title = R.string.auto_backup_stores,
        description = null,
        default = defaultBackupStores
    )


    // TODO ( after  3.0.0 ) - Bruh
    @SettingKey
    val numberOfBackupsToKeep = int(
        default = 2,
        title = null,
        description = null,
        allowedRange = 1..10
    )
}