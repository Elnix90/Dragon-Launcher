package org.elnix.dragonlauncher.icons

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.room.withTransaction
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.ICONS_TAG
import org.elnix.dragonlauncher.base.icons.ClockLayer
import org.elnix.dragonlauncher.base.icons.ClockSublayer
import org.elnix.dragonlauncher.base.icons.ClockSublayerRole
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.database.AppDatabase
import org.elnix.dragonlauncher.icons.compat.AdaptiveIconDrawableCompat
import org.elnix.dragonlauncher.icons.compat.toLauncherIcon
import org.elnix.dragonlauncher.icons.loaders.AppFilterIconPackInstaller
import org.elnix.dragonlauncher.icons.loaders.GrayscaleMapIconPackInstaller
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel
import kotlin.math.roundToInt

public class IconPackManager(
    private val ctx: Context,
    private val appDatabase: AppDatabase
) {
    public fun getInstalledIconPacks(): Flow<List<IconPack>> =
        appDatabase.iconDao().getInstalledIconPacks().map { packs ->
            packs.map { IconPack(it) }
        }

    public suspend fun getIconPack(packageName: String): IconPack? =
        withContext(Dispatchers.IO) {
            appDatabase.iconDao().getIconPack(packageName)?.let {
                IconPack(it)
            }
        }

    private var updateIconPacksMutex = Mutex()

    public suspend fun updateIconPacks(forceReinstall: Boolean = false): Boolean {
        var iconsHaveBeenUpdated = false
        updateIconPacksMutex.lock()
        val installers =
            listOf(
                AppFilterIconPackInstaller(ctx, appDatabase),
                GrayscaleMapIconPackInstaller(ctx, appDatabase)
            )
        val installedPacks = mutableListOf<IconPack>()
        for (installer in installers) {
            val iconPacks = installer.getInstalledIconPacks()
            for (pack in iconPacks) {
                if (forceReinstall || !installer.isInstalledAndUpToDate(pack)) {
                    installer.install(pack)
                    iconsHaveBeenUpdated = true
                } else {
                    logD(ICONS_TAG) { "Icon pack ${pack.packageName} is up to date" }
                }
            }
            installedPacks.addAll(iconPacks)
        }
        uninstallAllIconPacksExcept(installedPacks)
        updateIconPacksMutex.unlock()
        return iconsHaveBeenUpdated
    }

    private suspend fun uninstallAllIconPacksExcept(keep: List<IconPack>) {
        val dao = appDatabase.iconDao()
        appDatabase.withTransaction {
            dao.deleteIconsNotIn(keep.map { it.packageName })
            dao.deleteIconPacksNotIn(keep.map { it.packageName })
        }
    }

    public suspend fun getIcon(
        iconPack: String,
        packageName: String,
        activityName: String?,
        tint: Int?,
        allowThemed: Boolean = true
    ): LauncherIcon? =
        withContext(Dispatchers.IO) {
            val res =
                try {
                    ctx.packageManager.getResourcesForApplication(iconPack)
                } catch (e: PackageManager.NameNotFoundException) {
                    logE(ICONS_TAG, e) { "Icon pack package $iconPack not found!" }
                    return@withContext null
                }
            val activity = activityName?.let { ComponentName(packageName, it) }?.shortClassName
            val iconDao = appDatabase.iconDao()
            val icon =
                iconDao.getIcon(packageName, activity, iconPack)?.let { iconPackAppIcon(it) }
                    ?: return@withContext null

            when (icon) {
                is CalendarIcon -> {
                    return@withContext getIconPackCalendarIcon(icon, res, tint, allowThemed)
                }

                is AppIcon -> {
                    return@withContext getIconPackStaticIcon(icon, res, tint, allowThemed)
                }

                is ClockIcon -> {
                    return@withContext getIconPackClockIcon(icon, res, tint, allowThemed)
                }
            }
        }

    public suspend fun getIcon(
        iconPack: String,
        icon: IconPackAppIcon,
        tint: Int?,
        allowThemed: Boolean
    ): LauncherIcon? =
        withContext(Dispatchers.IO) {
            val res =
                try {
                    ctx.packageManager.getResourcesForApplication(iconPack)
                } catch (e: PackageManager.NameNotFoundException) {
                    logE(ICONS_TAG, e) { "Icon pack package $iconPack not found!" }
                    return@withContext null
                }
            when (icon) {
                is CalendarIcon -> {
                    return@withContext getIconPackCalendarIcon(icon, res, tint, allowThemed)
                }

                is AppIcon -> {
                    return@withContext getIconPackStaticIcon(icon, res, tint, allowThemed)
                }

                is ClockIcon -> {
                    return@withContext getIconPackClockIcon(icon, res, tint, allowThemed)
                }
            }
        }

    @SuppressLint("DiscouragedApi")
    public suspend fun generateIcon(
        ctx: Context,
        iconPack: String,
        baseIcon: Drawable,
        tint: Int?,
        size: Int
    ): LauncherIcon? =
        withContext(Dispatchers.IO) {
            val back = getIconBack(iconPack)
            val upon = getIconUpon(iconPack)
            val mask = getIconMask(iconPack)
            val scale = getPackScale(iconPack)

            if (back == null && upon == null && mask == null) {
                return@withContext null
            }

            val bitmap = createBitmap(size, size)

            val canvas = Canvas(bitmap)
            val paint = Paint()
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            paint.isDither = true

            var inBounds: Rect
            var outBounds: Rect

            val icon = baseIcon.toBitmap(width = size, height = size)

            inBounds = Rect(0, 0, icon.width, icon.height)
            outBounds =
                Rect(
                    (bitmap.width * (1 - scale) * 0.5).roundToInt(),
                    (bitmap.height * (1 - scale) * 0.5).roundToInt(),
                    (bitmap.width - bitmap.width * (1 - scale) * 0.5).roundToInt(),
                    (bitmap.height - bitmap.height * (1 - scale) * 0.5).roundToInt()
                )
            canvas.drawBitmap(icon, inBounds, outBounds, paint)

            val pm = ctx.packageManager
            val res =
                try {
                    pm.getResourcesForApplication(iconPack)
                } catch (_: Resources.NotFoundException) {
                    return@withContext null
                } catch (_: PackageManager.NameNotFoundException) {
                    return@withContext null
                }

            logD(ICONS_TAG) { "Generating icon tinted with: $tint for ressources: $res" }

            if (mask != null) {
                res.getIdentifier(mask, "drawable", iconPack).takeIf { it != 0 }?.let {
                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                    val maskDrawable =
                        try {
                            ResourcesCompat.getDrawable(res, it, null) ?: return@withContext null
                        } catch (_: Resources.NotFoundException) {
                            return@withContext null
                        }
                    val maskBmp = maskDrawable.toBitmap(size, size)
                    inBounds = Rect(0, 0, maskBmp.width, maskBmp.height)
                    outBounds = Rect(0, 0, bitmap.width, bitmap.height)
                    canvas.drawBitmap(maskBmp, inBounds, outBounds, paint)
                }
            }
            if (upon != null) {
                res.getIdentifier(upon, "drawable", iconPack).takeIf { it != 0 }?.let {
                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
                    val maskDrawable =
                        try {
                            ResourcesCompat.getDrawable(res, it, null) ?: return@withContext null
                        } catch (_: Resources.NotFoundException) {
                            return@withContext null
                        }
                    val maskBmp = maskDrawable.toBitmap(size, size)
                    inBounds = Rect(0, 0, maskBmp.width, maskBmp.height)
                    outBounds = Rect(0, 0, bitmap.width, bitmap.height)
                    canvas.drawBitmap(maskBmp, inBounds, outBounds, paint)
                }
            }
            if (back != null) {
                res.getIdentifier(back, "drawable", iconPack).takeIf { it != 0 }?.let {
                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
                    val maskDrawable =
                        try {
                            ResourcesCompat.getDrawable(res, it, null) ?: return@withContext null
                        } catch (_: Resources.NotFoundException) {
                            return@withContext null
                        }
                    val maskBmp = maskDrawable.toBitmap(size, size)
                    inBounds = Rect(0, 0, maskBmp.width, maskBmp.height)
                    outBounds = Rect(0, 0, bitmap.width, bitmap.height)
                    canvas.drawBitmap(maskBmp, inBounds, outBounds, paint)
                }
            }

            return@withContext StaticLauncherIcon(
                foregroundLayer =
                    StaticIconLayer(
                        icon = bitmap.toDrawable(ctx.resources),
                        scale = 1f,
                        tint = tint
                    ),
                backgroundLayer = TransparentLayer
            )
        }

    public suspend fun getAllIconPackIcons(componentName: ComponentName): List<IconPackAppIcon> {
        val iconDao = appDatabase.iconDao()
        return iconDao
            .getIconsFromAllPacks(componentName.packageName, componentName.shortClassName)
            .mapNotNull { iconPackAppIcon(it) }
    }

    private suspend fun getIconBack(iconPack: String): String? {
        val iconDao = appDatabase.iconDao()
        val iconbacks = iconDao.getIconBacks(iconPack)
        return iconbacks.randomOrNull()
    }

    private suspend fun getIconUpon(iconPack: String): String? {
        val iconDao = appDatabase.iconDao()
        val iconupons = iconDao.getIconUpons(iconPack)
        return iconupons.randomOrNull()
    }

    private suspend fun getIconMask(iconPack: String): String? {
        val iconDao = appDatabase.iconDao()
        val iconmasks = iconDao.getIconMasks(iconPack)
        return iconmasks.randomOrNull()
    }

    private suspend fun getPackScale(iconPack: String): Float {
        val iconDao = appDatabase.iconDao()
        return iconDao.getScale(iconPack) ?: 1f
    }

    @SuppressLint("DiscouragedApi")
    private fun getIconPackStaticIcon(
        icon: AppIcon,
        resources: Resources,
        tint: Int?,
        allowThemed: Boolean
    ): LauncherIcon? {
        val resId =
            resources.getIdentifier(icon.drawable, "drawable", icon.iconPack).takeIf { it != 0 }
                ?: return null

        val adaptiveIconCompat = AdaptiveIconDrawableCompat.from(resources, resId)
        if (adaptiveIconCompat != null) {
            return adaptiveIconCompat.toLauncherIcon(themed = allowThemed && icon.themed, tint = tint)
        }
        val drawable =
            try {
                ResourcesCompat.getDrawable(resources, resId, ctx.theme) ?: return null
            } catch (_: Resources.NotFoundException) {
                return null
            }
        val themed = icon.themed && allowThemed
        return when {
            themed && drawable is AdaptiveIconDrawable -> {
                if (isAtLeastApiLevel(33) && drawable.monochrome != null) {
                    StaticLauncherIcon(
                        foregroundLayer =
                            StaticIconLayer(
                                icon = drawable.monochrome!!,
                                scale = 1.5f,
                                tint = tint
                            ),
                        backgroundLayer = TransparentLayer
                    )
                } else {
                    StaticLauncherIcon(
                        foregroundLayer =
                            StaticIconLayer(
                                icon = drawable.foreground,
                                scale = 1.5f,
                                tint = tint
                            ),
                        backgroundLayer = TransparentLayer
                    )
                }
            }

            themed -> {
                StaticLauncherIcon(
                    foregroundLayer =
                        StaticIconLayer(
                            icon = drawable,
                            scale = 0.65f,
                            tint = tint
                        ),
                    backgroundLayer = TransparentLayer
                )
            }

            drawable is AdaptiveIconDrawable -> {
                StaticLauncherIcon(
                    foregroundLayer =
                        drawable.foreground?.let {
                            StaticIconLayer(
                                icon = it,
                                scale = 1.5f,
                                tint = tint
                            )
                        } ?: TransparentLayer,
                    backgroundLayer =
                        drawable.background?.let {
                            StaticIconLayer(
                                icon = it,
                                scale = 1.5f,
                                tint = tint
                            )
                        } ?: TransparentLayer
                )
            }

            else -> {
                StaticLauncherIcon(
                    foregroundLayer =
                        StaticIconLayer(
                            icon = drawable,
                            scale = 1f,
                            tint = tint
                        ),
                    backgroundLayer = TransparentLayer
                )
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun getIconPackCalendarIcon(
        icon: CalendarIcon,
        resources: Resources,
        tint: Int?,
        allowThemed: Boolean
    ): LauncherIcon? {
        val drawableIds =
            icon.drawables
                .map {
                    val id = resources.getIdentifier(it, "drawable", icon.iconPack)
                    if (id == 0) return null
                    id
                }.toIntArray()

        if (icon.themed && allowThemed) {
            return ThemedDynamicCalendarIcon(
                resources = resources,
                resourceIds = drawableIds,
                tint = tint
            )
        }
        return DynamicCalendarIcon(
            resources = resources,
            resourceIds = drawableIds,
            tint = tint
        )
    }

    @SuppressLint("DiscouragedApi")
    private fun getIconPackClockIcon(
        icon: ClockIcon,
        resources: Resources,
        tint: Int?,
        allowThemed: Boolean
    ): LauncherIcon? {
        val drawableId =
            try {
                resources.getIdentifier(icon.drawable, "drawable", icon.iconPack).takeIf { it != 0 }
                    ?: return null
            } catch (_: Resources.NotFoundException) {
                return null
            }
        val adaptiveIconCompat = AdaptiveIconDrawableCompat.from(resources, drawableId)
        if (adaptiveIconCompat != null) {
            return adaptiveIconCompat.toLauncherIcon(themed = icon.themed && allowThemed, tint = tint, clock = icon.config)
        }
        val drawable =
            try {
                ResourcesCompat.getDrawable(resources, drawableId, null)
            } catch (_: Resources.NotFoundException) {
                null
            } ?: return null

        val background = (drawable as? AdaptiveIconDrawable)?.background
        val foreground = (drawable as? AdaptiveIconDrawable)?.foreground ?: drawable

        if (foreground !is LayerDrawable) return null

        val layers =
            (0 until foreground.numberOfLayers).map {
                val drw = foreground.getDrawable(it)
                ClockSublayer(
                    drawable = drw,
                    role =
                        when (it) {
                            icon.config.hourLayer -> ClockSublayerRole.Hour
                            icon.config.minuteLayer -> ClockSublayerRole.Minute
                            icon.config.secondLayer -> ClockSublayerRole.Second
                            else -> ClockSublayerRole.Static
                        }
                )
            }

        val themed = icon.themed && allowThemed

        return when {
            themed && drawable is AdaptiveIconDrawable -> {
                StaticLauncherIcon(
                    foregroundLayer =
                        ClockLayer(
                            defaultHour = icon.config.defaultHour,
                            defaultMinute = icon.config.defaultMinute,
                            defaultSecond = icon.config.defaultSecond,
                            sublayers = layers,
                            scale = 1.5f,
                            tint = tint
                        ),
                    backgroundLayer = TransparentLayer
                )
            }

            themed -> {
                StaticLauncherIcon(
                    foregroundLayer =
                        ClockLayer(
                            defaultHour = icon.config.defaultHour,
                            defaultMinute = icon.config.defaultMinute,
                            defaultSecond = icon.config.defaultSecond,
                            sublayers = layers,
                            scale = 1f,
                            tint = tint
                        ),
                    backgroundLayer = TransparentLayer
                )
            }

            drawable is AdaptiveIconDrawable -> {
                StaticLauncherIcon(
                    foregroundLayer =
                        ClockLayer(
                            defaultHour = icon.config.defaultHour,
                            defaultMinute = icon.config.defaultMinute,
                            defaultSecond = icon.config.defaultSecond,
                            sublayers = layers,
                            scale = 1.5f,
                            tint = tint
                        ),
                    backgroundLayer =
                        StaticIconLayer(
                            icon = background!!,
                            scale = 1.5f,
                            tint = tint
                        )
                )
            }

            else -> {
                StaticLauncherIcon(
                    foregroundLayer =
                        ClockLayer(
                            defaultHour = icon.config.defaultHour,
                            defaultMinute = icon.config.defaultMinute,
                            defaultSecond = icon.config.defaultSecond,
                            sublayers = layers,
                            scale = 1f,
                            tint = tint
                        ),
                    backgroundLayer = TransparentLayer
                )
            }
        }
    }

    public suspend fun searchIconPackIcon(query: String, iconPack: IconPack?): List<IconPackAppIcon> {
        val iconDao = appDatabase.iconDao()
        val drawableQuery = query.replace(" ", "_").lowercase()
        return iconDao
            .searchIconPackIcons(
                drawableQuery = "%$drawableQuery%",
                nameQuery = "%$query%",
                iconPack = iconPack?.packageName
            ).mapNotNull {
                iconPackAppIcon(it)
            }.distinct()
    }
}
