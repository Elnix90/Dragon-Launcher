package org.elnix.dragonlauncher.settings.stores.map

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


    val autoBackupEnabled = boolean(
        key = "autoBackupEnabled",
        default = false
    )

    val autoBackupUri = string(
        key = "autoBackupUri",
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

    val backupStores = stringSet(
        key = "backupStores",
        default = defaultBackupStores
    )



    // TODO ( after  3.0.0 )
    val numberOfBackupsToKeep = int(
        key = "numberOfBackupsToKeep",
        default = 2,
        allowedRange = 1..10
    )
}