package org.elnix.dragonlauncher.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "IconPack")
public data class IconPackEntity(
        val name: String,
        @PrimaryKey val packageName: String,
        val version: String,
        var scale: Float = 1f,
        val themed: Boolean = false,
)