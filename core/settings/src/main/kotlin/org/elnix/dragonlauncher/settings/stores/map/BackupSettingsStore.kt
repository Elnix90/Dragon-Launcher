package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.StringSetSettingObject
import io.github.elnix90.core.objects.StringSettingObject
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.objects.stringSet
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
public object BackupSettingsStore : MapSettingsStore() {

    @SettingKey
    public val autoBackupEnabled: BooleanSettingObject = boolean(
        title = R.string.automatic_backups,
        description = R.string.auto_backup_desc,
        icon = R.drawable.save,
        default = false
    )

    @SettingKey
    public val autoBackupUri: StringSettingObject = string(
        default = ""
    )


    /**
     * I use an empty set because it causes failures in runtime during the resolution of AllsStores for some reason
     */
    @SettingKey
    public val backupStores: StringSetSettingObject = stringSet(
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