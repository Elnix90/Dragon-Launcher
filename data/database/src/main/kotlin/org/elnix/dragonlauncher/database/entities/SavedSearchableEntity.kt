package org.elnix.dragonlauncher.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Searchable")
public data class SavedSearchableEntity(
    @PrimaryKey val key: String,
    val type: String,
    @ColumnInfo(name = "searchable") val serializedSearchable: String,
    @ColumnInfo(defaultValue = "0") val launchCount: Int,
    @ColumnInfo(defaultValue = "0") val pinPosition: Int,
    @ColumnInfo(name = "hidden", defaultValue = "0") val visibility: Int,
    @ColumnInfo(defaultValue = "0.0") val weight: Double
)

public data class SavedSearchableUpdatePinEntity(
    val key: String,
    val type: String,
    @ColumnInfo(name = "searchable") val serializedSearchable: String,
    val pinPosition: Int? = null
)

public data class SavedSearchableUpdateContentEntity(
    val key: String,
    val type: String,
    @ColumnInfo(name = "searchable") val serializedSearchable: String
)
