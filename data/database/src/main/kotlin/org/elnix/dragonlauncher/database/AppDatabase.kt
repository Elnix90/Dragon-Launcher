@file:Suppress("ClassName")

package org.elnix.dragonlauncher.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.elnix.dragonlauncher.database.daos.IconDao
import org.elnix.dragonlauncher.database.entities.IconEntity
import org.elnix.dragonlauncher.database.entities.IconPackEntity
import org.elnix.dragonlauncher.database.entities.SavedSearchableEntity

@Database(
    entities = [
        SavedSearchableEntity::class,
        IconEntity::class,
        IconPackEntity::class,
    ], version = 32, exportSchema = true
)
@TypeConverters(ComponentNameConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun iconDao(): IconDao
//    abstract fun searchableDao(): SearchableDao
//
//    abstract fun backupDao(): BackupRestoreDao
//    abstract fun customAttrsDao(): CustomAttrsDao

    companion object {
        private var _instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            val instance = _instance
                ?: Room
                    .databaseBuilder(context.applicationContext, AppDatabase::class.java, "room")
                    .build()
            if (_instance == null) _instance = instance
            return instance
        }
    }
}
