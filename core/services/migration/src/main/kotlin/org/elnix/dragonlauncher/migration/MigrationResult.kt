package org.elnix.dragonlauncher.migration

/**
 * Represents the outcome of a settings migration operation.
 *
 * @param success Whether the migration completed without critical errors.
 * @param migratedStores Set of store names that were successfully migrated.
 * @param skippedStores Set of store names that were skipped due to missing data or mapping.
 * @param errors List of error messages encountered during migration.
 * @param message Human-readable summary of the migration result.
 */
public data class MigrationResult(
    public val success: Boolean,
    public val migratedStores: Set<String> = emptySet(),
    public val skippedStores: Set<String> = emptySet(),
    public val errors: List<String> = emptyList(),
    public val message: String = ""
) {
    public companion object {
        /**
         * Creates a successful [MigrationResult] with the given migrated and skipped stores.
         *
         * @param migrated Set of stores that were successfully migrated.
         * @param skipped Set of stores that were skipped.
         * @return A success result.
         */
        public fun success(migrated: Set<String>, skipped: Set<String> = emptySet()): MigrationResult =
            MigrationResult(
                success = true,
                migratedStores = migrated,
                skippedStores = skipped,
                message = "Migrated: ${migrated.size} stores, skipped: ${skipped.size}"
            )

        /**
         * Creates a failed [MigrationResult] with the given error description.
         *
         * @param error A primary error message.
         * @param errors Additional error messages.
         * @return A failure result.
         */
        public fun failure(error: String, errors: List<String> = listOf(error)): MigrationResult =
            MigrationResult(
                success = false,
                errors = errors,
                message = "Migration failed: $error"
            )
    }
}
