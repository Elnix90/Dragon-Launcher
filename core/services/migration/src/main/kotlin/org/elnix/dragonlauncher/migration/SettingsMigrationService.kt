package org.elnix.dragonlauncher.migration

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.elnix90.logging.BACKUP_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logE
import io.github.elnix90.logging.logI
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central service for migrating app settings from version 3.2.2 to 4.0.0.
 *
 * Provides two migration strategies:
 * 1. **Silent auto-migration**: Reads old DataStore files and writes to the new single DataStore.
 *    This happens transparently when the user upgrades.
 * 2. **Manual JSON-based migration**: Reads an exported legacy backup JSON and maps stores/keys
 *    to the new format. This is used when restoring from a manual backup.
 *
 * Usage:
 * ```
 * // Auto-migration (call on app startup for version upgrades)
 * migrationService.attemptAutoMigration(context)
 *
 * // Manual migration from a backup JSON
 * migrationService.migrateFromBackupJson(context, jsonString)
 * ```
 */
@Singleton
public class SettingsMigrationService @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val dataStoreMigrator = DataStoreMigrator()
    private val backupJsonMigrator = LegacyBackupJsonMigrator()

    /**
     * Attempts silent auto-migration by reading old DataStore files directly.
     *
     * This method should be called once on app startup during the first launch
     * after upgrading from 3.2.2 to 4.0.0.
     *
     * @param ctx Android context (typically Application).
     * @param onComplete Callback invoked with the migration result on the main thread.
     */
    public suspend fun attemptAutoMigration(
        ctx: Context,
        onComplete: suspend (MigrationResult) -> Unit
    ) {
        try {
            logI(BACKUP_TAG) { "Checking if auto-migration is needed..." }

            if (!dataStoreMigrator.isMigrationNeeded(ctx)) {
                logD(BACKUP_TAG) { "No migration needed" }
                onComplete(
                    MigrationResult(
                        success = true,
                        message = "No migration needed - already up to date"
                    )
                )
                return
            }

            logI(BACKUP_TAG) { "Starting auto-migration from old DataStore files..." }
            val result = dataStoreMigrator.migrateFromOldDataStores(ctx)

            logResult(result)

            onComplete(result)
        } catch (e: Exception) {
            logE(BACKUP_TAG, e) { "Auto-migration failed with exception" }
            onComplete(MigrationResult.failure(e.message ?: "Unknown error"))
        }
    }


    /**
     * Logs the contents of a [MigrationResult] using the structured logging system.
     *
     * @param result The migration result to log.
     */
    public fun logResult(result: MigrationResult) {
        if (result.success) {
            logI(BACKUP_TAG) { "Auto-migration completed: ${result.message}" }
        } else {
            logI(BACKUP_TAG) { "Auto-migration skipped or failed: ${result.message}" }
        }

        if (result.migratedStores.isNotEmpty()) {
            logD(BACKUP_TAG) { "Migrated: ${result.migratedStores.joinToString()}" }
        }
        if (result.skippedStores.isNotEmpty()) {
            logD(BACKUP_TAG) { "Skipped: ${result.skippedStores.joinToString()}" }
        }
        if (result.errors.isNotEmpty()) {
            logD(BACKUP_TAG) { "Errors: ${result.errors.joinToString()}" }
        }
    }

    /**
     * Migrates settings from an exported legacy 3.2.2 backup JSON string.
     *
     * This is the fallback approach: the user provides their old backup file,
     * and this method maps all stores and keys to the new format.
     *
     * @param ctx Android context (typically Application).
     * @param legacyJson Raw JSON string from an old backup.
     * @return [MigrationResult] describing what was migrated.
     */
    public suspend fun migrateFromBackupJson(
        ctx: Context,
        legacyJson: String
    ): MigrationResult {
        return backupJsonMigrator.migrateFromJson(ctx, legacyJson)
    }

    /**
     * Checks whether the given JSON string represents a legacy 3.2.2 backup
     * that needs migration.
     *
     * Detection is based on the `app_version` field or the presence of
     * known-old top-level keys (`new_actions`, `swipe_map`).
     *
     * @param jsonString The raw JSON string to check.
     * @return `true` if this appears to be a legacy backup.
     */
    public fun isLegacyBackup(jsonString: String): Boolean {
        return try {
            val json = JSONObject(jsonString)
            backupJsonMigrator.isLegacyBackup(json)
        } catch (_: Exception) {
            false
        }
    }

    public fun isMigrationNeeded(ctx: Context): Boolean = dataStoreMigrator.isMigrationNeeded(ctx)
}
