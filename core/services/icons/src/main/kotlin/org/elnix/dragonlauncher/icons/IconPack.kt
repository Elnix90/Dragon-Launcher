package org.elnix.dragonlauncher.icons

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import androidx.compose.runtime.Immutable
import org.elnix.dragonlauncher.database.entities.IconPackEntity

@Immutable
public data class IconPack(
    val name: String,
    val packageName: String,
    val version: String,
    val scale: Float = 1f,
    val themed: Boolean = false
) {
    public constructor(entity: IconPackEntity) : this(
        name = entity.name,
        packageName = entity.packageName,
        version = entity.version,
        scale = entity.scale,
        themed = entity.themed
    )

    internal constructor(
        context: Context,
        resolveInfo: ResolveInfo,
        themed: Boolean = false
    ) : this(
        name = resolveInfo.loadLabel(context.packageManager).toString(),
        packageName = resolveInfo.activityInfo.packageName,
        version =
            context.packageManager
                .getPackageInfo(resolveInfo.activityInfo.packageName, 0)
                .let {
                    "${it.versionName} (${it.versionCode})"
                },
        themed = themed
    )

    internal constructor(
        context: Context,
        packageInfo: PackageInfo,
        themed: Boolean = false
    ) : this(
        name = packageInfo.applicationInfo!!.loadLabel(context.packageManager).toString(),
        packageName = packageInfo.packageName,
        version =
            context.packageManager.getPackageInfo(packageInfo.packageName, 0).let {
                "${it.versionName} (${it.versionCode})"
            },
        themed = themed
    )

    public fun toDatabaseEntity(): IconPackEntity =
        IconPackEntity(
            name = name,
            scale = scale,
            version = version,
            packageName = packageName,
            themed = themed
        )
}
