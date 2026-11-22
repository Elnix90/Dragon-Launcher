package org.elnix.dragonlauncher.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.elnix.dragonlauncher.database.daos.IconDao
import org.elnix.dragonlauncher.database.entities.IconEntity
import org.elnix.dragonlauncher.database.entities.IconPackEntity
import org.elnix.dragonlauncher.database.entities.SavedSearchableEntity

@Database(
	entities = [
		SavedSearchableEntity::class,
		IconEntity::class,
		IconPackEntity::class
	],
	version = 2,
	exportSchema = true
)
@TypeConverters(ComponentNameConverter::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun iconDao(): IconDao

	companion object {
		@Volatile
		private var instance: AppDatabase? = null
		private val MIGRATION_1_2 =
			object : Migration(1, 2) {
				override fun migrate(db: SupportSQLiteDatabase) {
					db.execSQL("ALTER TABLE IconPack ADD COLUMN importerVersion INTEGER NOT NULL DEFAULT 0")
				}
			}

		@JvmStatic
		fun getInstance(ctx: Context): AppDatabase {
			val instance =
				instance
					?: Room
						.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "room")
						.addMigrations(MIGRATION_1_2)
						.build()
			if (Companion.instance == null) Companion.instance = instance
			return instance
		}
	}
}
