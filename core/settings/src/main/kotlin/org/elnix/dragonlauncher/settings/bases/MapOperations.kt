package org.elnix.dragonlauncher.settings.bases

import android.content.Context
import org.elnix.dragonlauncher.settings.bases.objects.SettingObject

@Suppress("NOTHING_TO_INLINE")
internal suspend inline fun MutableMap<String, Any>.putIfNonDefault(
    ctx: Context,
    settingObject: SettingObject<*, *>
) {
    if (settingObject.isNotNullOrDefault(ctx)) {
        put(settingObject.key, settingObject.getEncoded(ctx) as Any)
    }
}

@Suppress("NOTHING_TO_INLINE")
internal suspend inline fun MutableMap<String, Any>.putIfNotNull(
    ctx: Context,
    settingObject: SettingObject<*,*>
) {
    val value = settingObject.get(ctx)

    if (value != null) {
        put(settingObject.key, settingObject.getEncoded(ctx) as Any)
    }
}

internal suspend inline fun SettingObject<*, *>.isNotNullOrDefault(
    ctx: Context,
): Boolean {
    val value = this.get(ctx)
    return value != null && value != this.default
}