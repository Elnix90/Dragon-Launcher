package org.elnix.dragonlauncher.icons.loaders

import androidx.room.withTransaction
import org.elnix.dragonlauncher.database.AppDatabase
import org.elnix.dragonlauncher.icons.IconPack
import org.elnix.dragonlauncher.icons.IconPackComponent

internal const val ICON_PACK_IMPORTER_VERSION = 2

internal abstract class IconPackInstaller(
	private val database: AppDatabase
) {
	suspend fun install(
		iconPack: IconPack,
		clearExisting: Boolean = true
	) {
		var pack = iconPack
		val dao = database.iconDao()
		database.withTransaction {
			if (clearExisting) {
				dao.deleteIconPack(iconPack.toDatabaseEntity())
				dao.deleteIcons(iconPack.packageName)
			} else {
				pack = dao.getIconPack(iconPack.packageName)?.let(::IconPack) ?: iconPack
			}
			val icons = mutableListOf<IconPackComponent>()
			val installerScope =
				object : IconPackInstallerScope {
					override suspend fun addIcon(icon: IconPackComponent) {
						icons.add(icon)
						if (icons.size >= 100) {
							dao.insertAll(icons.map { it.toDatabaseEntity() })
							icons.clear()
						}
					}

					override suspend fun updatePackInfo(update: (IconPack) -> IconPack) {
						pack = update(pack)
					}
				}
			installerScope.buildIconPack(iconPack)
			if (icons.isNotEmpty()) dao.insertAll(icons.map { it.toDatabaseEntity() })
			dao.installIconPack(pack.toDatabaseEntity())
		}
	}

	abstract suspend fun IconPackInstallerScope.buildIconPack(iconPack: IconPack)

	abstract fun getInstalledIconPacks(): List<IconPack>

	suspend fun isInstalledAndUpToDate(iconPack: IconPack): Boolean {
		val dao = database.iconDao()
		val installed = dao.getIconPack(iconPack.packageName) ?: return false
		return installed.version == iconPack.version && installed.importerVersion == ICON_PACK_IMPORTER_VERSION
	}
}

interface IconPackInstallerScope {
	suspend fun addIcon(icon: IconPackComponent)

	suspend fun updatePackInfo(update: (IconPack) -> IconPack)
}
