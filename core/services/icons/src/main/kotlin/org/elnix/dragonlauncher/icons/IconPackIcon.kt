package org.elnix.dragonlauncher.icons

import org.elnix.dragonlauncher.database.entities.IconEntity
import org.elnix.dragonlauncher.icons.compat.ClockIconConfig
import org.elnix.dragonlauncher.ktx.jsonObjectOf
import org.json.JSONObject

sealed interface IconPackComponent {
    val iconPack: String
    fun toDatabaseEntity(): IconEntity
}

sealed interface IconPackAppIcon: IconPackComponent {
    val packageName: String?
    val activityName: String?
    val name: String?
    val themed: Boolean
    val tint: Int?
}

data class IconBack(
    val drawable: String,
    override val iconPack: String,
): IconPackComponent {
    override fun toDatabaseEntity(): IconEntity {
        return IconEntity(
            type = "iconback",
            drawable = drawable,
            iconPack = iconPack,
        )
    }
}

data class IconUpon(
    val drawable: String,
    override val iconPack: String,
): IconPackComponent {
    override fun toDatabaseEntity(): IconEntity {
        return IconEntity(
            type = "iconupon",
            drawable = drawable,
            iconPack = iconPack,
        )
    }
}

data class IconMask(
    val drawable: String,
    override val iconPack: String,
): IconPackComponent {
    override fun toDatabaseEntity(): IconEntity {
        return IconEntity(
            type = "iconmask",
            drawable = drawable,
            iconPack = iconPack,
        )
    }
}

data class AppIcon(
    val drawable: String,
    override val iconPack: String,
    override val packageName: String? = null,
    override val activityName: String? = null,
    override val name: String? = null,
    override val themed: Boolean = false,
    override val tint: Int? = null
): IconPackComponent, IconPackAppIcon {
    override fun toDatabaseEntity(): IconEntity {
        return IconEntity(
            type = "app",
            packageName = packageName,
            activityName = activityName,
            drawable = drawable,
            name = name,
            iconPack = iconPack,
            themed = themed,
        )
    }
}

data class CalendarIcon(
    val drawables: List<String>,
    override val iconPack: String,
    override val packageName: String?,
    override val activityName: String? = null,
    override val name: String? = null,
    override val themed: Boolean = false,
    override val tint: Int? = null
): IconPackComponent, IconPackAppIcon {
    override fun toDatabaseEntity(): IconEntity {
        return IconEntity(
            type = "calendar",
            drawable = drawables.joinToString(","),
            iconPack = iconPack,
            packageName = packageName,
            activityName = activityName,
            name = name,
            themed = themed,
        )
    }
}


data class ClockIcon(
    val drawable: String,
    override val iconPack: String,
    override val packageName: String? = null,
    override val activityName: String? = null,
    override val name: String? = null,
    override val themed: Boolean,
    override val tint: Int? = null,
    val config: ClockIconConfig,
): IconPackComponent, IconPackAppIcon {
    override fun toDatabaseEntity(): IconEntity {
        return IconEntity(
            type = "clock",
            packageName = packageName,
            activityName = activityName,
            drawable = drawable,
            name = name,
            iconPack = iconPack,
            themed = themed,
            extras = jsonObjectOf(
                "defaultSecond" to config.defaultSecond,
                "defaultMinute" to config.defaultMinute,
                "defaultHour" to config.defaultHour,
                "hourLayer" to config.hourLayer,
                "minuteLayer" to config.minuteLayer,
                "secondLayer" to config.secondLayer,
            ).toString(),
        )
    }
}

fun Icon(entity: IconEntity): IconPackComponent? {
    return when(entity.type) {
        "iconback" -> IconBack(
            drawable = entity.drawable ?: return null,
            iconPack = entity.iconPack,
        )
        "iconupon" -> IconUpon(
            drawable = entity.drawable ?: return null,
            iconPack = entity.iconPack,
        )
        "iconmask" -> IconMask(
            drawable = entity.drawable ?: return null,
            iconPack = entity.iconPack,
        )
        "app" -> AppIcon(
            drawable = entity.drawable ?: return null,
            iconPack = entity.iconPack,
            packageName = entity.packageName,
            activityName = entity.activityName,
            themed = entity.themed,
            name = entity.name,
        )
        "calendar" -> CalendarIcon(
            drawables = entity.drawable?.split(",") ?: return null,
            iconPack = entity.iconPack,
            themed = entity.themed,
            packageName = entity.packageName,
            activityName = entity.activityName,
            name = entity.name,
        )
        "clock" -> {
            val config = JSONObject(entity.extras ?: return null)
            ClockIcon(
                drawable = entity.drawable!!,
                iconPack = entity.iconPack,
                packageName = entity.packageName,
                name = entity.name,
                activityName = entity.activityName,
                themed = entity.themed,
                config = ClockIconConfig(
                    defaultSecond = config.optInt("defaultSecond", 0),
                    defaultMinute = config.optInt("defaultMinute", 0),
                    defaultHour = config.optInt("defaultHour", 0),
                    hourLayer = config.optInt("hourLayer", 0),
                    minuteLayer = config.optInt("minuteLayer", 0),
                    secondLayer = config.optInt("secondLayer", 0),
                )
            )
        }
        else -> null
    }
}

fun IconPackAppIcon(entity: IconEntity): IconPackAppIcon? {
    if (entity.type != "app" && entity.type != "calendar" && entity.type != "clock") return null
    return Icon(entity) as? IconPackAppIcon
}
