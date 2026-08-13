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
    ], version = 1, exportSchema = true
)
@TypeConverters(ComponentNameConverter::class)
public abstract class AppDatabase : RoomDatabase() {
    public abstract fun iconDao(): IconDao

    public companion object {
        private var _instance: AppDatabase? = null
        public fun getInstance(context: Context): AppDatabase {
            val instance = _instance
                ?: Room
                    .databaseBuilder(context.applicationContext, AppDatabase::class.java, "room")
                    .build()
            if (_instance == null) _instance = instance
            return instance
        }
    }
}
