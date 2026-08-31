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
        IconPackEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(ComponentNameConverter::class)
public abstract class AppDatabase : RoomDatabase() {
    public abstract fun iconDao(): IconDao

    public companion object {
        @Volatile
        private var instance: AppDatabase? = null

        @JvmStatic
        public fun getInstance(ctx: Context): AppDatabase {
            val instance =
                instance
                    ?: Room
                        .databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "room")
                        .build()
            if (Companion.instance == null) Companion.instance = instance
            return instance
        }
    }
}
