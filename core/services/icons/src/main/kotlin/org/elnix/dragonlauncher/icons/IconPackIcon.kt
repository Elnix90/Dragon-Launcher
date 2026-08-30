package org.elnix.dragonlauncher.icons

import org.elnix.dragonlauncher.database.entities.IconEntity
import org.elnix.dragonlauncher.icons.compat.ClockIconConfig
import org.elnix.dragonlauncher.ktx.jsonObjectOf
import org.json.JSONObject

public sealed interface IconPackComponent {
    public val iconPack: String

    public fun toDatabaseEntity(): IconEntity
}

public sealed interface IconPackAppIcon : IconPackComponent {
    public val packageName: String?
    public val activityName: String?
    public val name: String?
    public val themed: Boolean
    public val tint: Int?
}

public data class IconBack(
    val drawable: String,
    override val iconPack: String
) : IconPackComponent {
    override fun toDatabaseEntity(): IconEntity =
        IconEntity(
            type = "iconback",
            drawable = drawable,
            iconPack = iconPack
        )
}

public data class IconUpon(
    val drawable: String,
    override val iconPack: String
) : IconPackComponent {
    override fun toDatabaseEntity(): IconEntity =
        IconEntity(
            type = "iconupon",
            drawable = drawable,
            iconPack = iconPack
        )
}

public data class IconMask(
    val drawable: String,
    override val iconPack: String
) : IconPackComponent {
    override fun toDatabaseEntity(): IconEntity =
        IconEntity(
            type = "iconmask",
            drawable = drawable,
            iconPack = iconPack
        )
}

public data class AppIcon(
    val drawable: String,
    override val iconPack: String,
    override val packageName: String? = null,
    override val activityName: String? = null,
    override val name: String? = null,
    override val themed: Boolean = false,
    override val tint: Int? = null
) : IconPackComponent,
    IconPackAppIcon {
    override fun toDatabaseEntity(): IconEntity =
        IconEntity(
            type = "app",
            packageName = packageName,
            activityName = activityName,
            drawable = drawable,
            name = name,
            iconPack = iconPack,
            themed = themed
        )
}

public data class CalendarIcon(
    val drawables: List<String>,
    override val iconPack: String,
    override val packageName: String?,
    override val activityName: String? = null,
    override val name: String? = null,
    override val themed: Boolean = false,
    override val tint: Int? = null
) : IconPackComponent,
    IconPackAppIcon {
    override fun toDatabaseEntity(): IconEntity =
        IconEntity(
            type = "calendar",
            drawable = drawables.joinToString(","),
            iconPack = iconPack,
            packageName = packageName,
            activityName = activityName,
            name = name,
            themed = themed
        )
}

public data class ClockIcon(
    val drawable: String,
    override val iconPack: String,
    override val packageName: String? = null,
    override val activityName: String? = null,
    override val name: String? = null,
    override val themed: Boolean,
    override val tint: Int? = null,
    val config: ClockIconConfig
) : IconPackComponent,
    IconPackAppIcon {
    override fun toDatabaseEntity(): IconEntity =
        IconEntity(
            type = "clock",
            packageName = packageName,
            activityName = activityName,
            drawable = drawable,
            name = name,
            iconPack = iconPack,
            themed = themed,
            extras =
                jsonObjectOf(
                    "defaultSecond" to config.defaultSecond,
                    "defaultMinute" to config.defaultMinute,
                    "defaultHour" to config.defaultHour,
                    "hourLayer" to config.hourLayer,
                    "minuteLayer" to config.minuteLayer,
                    "secondLayer" to config.secondLayer
                ).toString()
        )
}

public fun icon(entity: IconEntity): IconPackComponent? {
    return when (entity.type) {
        "iconback" ->
            IconBack(
                drawable = entity.drawable ?: return null,
                iconPack = entity.iconPack
            )
        "iconupon" ->
            IconUpon(
                drawable = entity.drawable ?: return null,
                iconPack = entity.iconPack
            )
        "iconmask" ->
            IconMask(
                drawable = entity.drawable ?: return null,
                iconPack = entity.iconPack
            )
        "app" ->
            AppIcon(
                drawable = entity.drawable ?: return null,
                iconPack = entity.iconPack,
                packageName = entity.packageName,
                activityName = entity.activityName,
                themed = entity.themed,
                name = entity.name
            )
        "calendar" ->
            CalendarIcon(
                drawables = entity.drawable?.split(",") ?: return null,
                iconPack = entity.iconPack,
                themed = entity.themed,
                packageName = entity.packageName,
                activityName = entity.activityName,
                name = entity.name
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
                config =
                    ClockIconConfig(
                        defaultSecond = config.optInt("defaultSecond", 0),
                        defaultMinute = config.optInt("defaultMinute", 0),
                        defaultHour = config.optInt("defaultHour", 0),
                        hourLayer = config.optInt("hourLayer", 0),
                        minuteLayer = config.optInt("minuteLayer", 0),
                        secondLayer = config.optInt("secondLayer", 0)
                    )
            )
        }
        else -> null
    }
}

public fun iconPackAppIcon(entity: IconEntity): IconPackAppIcon? {
    if (entity.type != "app" && entity.type != "calendar" && entity.type != "clock") return null
    return icon(entity) as? IconPackAppIcon
}
