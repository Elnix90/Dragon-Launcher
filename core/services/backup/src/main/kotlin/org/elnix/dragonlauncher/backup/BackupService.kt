package org.elnix.dragonlauncher.backup

import android.content.Context
import androidx.core.net.toUri
import io.github.elnix90.core.SettingsBackupManager.exportSettings
import io.github.elnix90.logging.logE
import io.github.elnix90.logging.logI
import io.github.elnix90.logging.logV
import io.github.elnix90.logging.logW
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.BACKUP_TAG
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.ktx.hasUriReadWritePermission
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.settings.backupableStores
import org.elnix.dragonlauncher.settings.stores.map.BackupSettingsStore
import org.elnix.dragonlauncher.settings.toSettingsStoreList
import kotlin.time.Duration.Companion.milliseconds

public interface BackupService {
	public val result: SettingFlow<BackupResult?>

	public fun commandBackup()
}

@OptIn(FlowPreview::class)
internal class BackupServiceIml(
	private val ctx: Context
) : BackupService {
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	override val result: SettingFlow<BackupResult?> = SettingFlow(null)
	private val backupTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

	init {
		scope.launch {
			backupTrigger
				.debounce(5000.milliseconds)
				.collect {
					performBackup()
				}
		}
	}

	override fun commandBackup() {
		backupTrigger.tryEmit(Unit)
	}

	private suspend fun performBackup() {
		if (!BackupSettingsStore.autoBackupEnabled.get(ctx)) {
			logV(BACKUP_TAG) { "Auto-backup disabled" }
			return
		}
		val uriString = BackupSettingsStore.autoBackupUri.get(ctx)
		if (uriString.isBlank()) {
			logW(BACKUP_TAG) { "No backup URI set" }
			return
		}
		val uri = uriString.toUri()
		if (!ctx.hasUriReadWritePermission(uri)) {
			logW(BACKUP_TAG) { "URI permission expired!" }
			ctx.showToast("Auto-backup URI expired. Please reselect file.")
			return
		}
		val selectedStores =
			BackupSettingsStore.backupStores
				.get(ctx)
				.takeIf { it.isNotEmpty() }
				?: backupableStores.mapTo(mutableSetOf()) { it.name }

		try {
			exportSettings(ctx, uri, selectedStores.toSettingsStoreList())
			logI(BACKUP_TAG) { "Auto-backup completed!" }
		} catch (e: Throwable) {
			logE(BACKUP_TAG, e) { "Auto-backup failed" }
			ctx.showToast("Auto backup failed: $e")
		}
	}
}

public data class BackupResult(
	val export: Boolean,
	val error: Boolean,
	val title: String,
	val message: String = ""
)
