package org.elnix.dragonlauncher.database

import android.content.ComponentName
import androidx.room.TypeConverter

internal class ComponentNameConverter {
    @TypeConverter
    fun toString(componentName: ComponentName?): String? = componentName?.flattenToString()

    @TypeConverter
    fun toComponentName(string: String?): ComponentName? {
        string ?: return null
        return ComponentName.unflattenFromString(string)
    }
}
