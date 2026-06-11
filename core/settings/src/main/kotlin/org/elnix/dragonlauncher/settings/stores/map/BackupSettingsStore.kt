package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.StringSetSettingObject.Companion.stringSet
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object BackupSettingsStore : MapSettingsStore(DataStoreName.BACKUP) {

    override val ALL: List<BaseSettingObject<*, *>>
        get() = listOf(
            this.autoBackupEnabled,
            this.autoBackupUri,
            this.backupStores
        )


    val autoBackupEnabled by boolean(
        title = R.string.automatic_backups,
        description = R.string.auto_backup_desc,
        default = false
    )

    val autoBackupUri by string(
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

    val backupStores by stringSet(
        title = R.string.auto_backup_stores,
        description = null,
        default = defaultBackupStores
    )


    // TODO ( after  3.0.0 ) - Bruh
    val numberOfBackupsToKeep by int(
        default = 2,
        title = null,
        description = null,
        allowedRange = 1..10
    )
}