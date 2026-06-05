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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.icons.ClockLayer
import org.elnix.dragonlauncher.base.icons.ClockSublayer
import org.elnix.dragonlauncher.base.icons.ClockSublayerRole
import org.elnix.dragonlauncher.base.icons.ColorLayer
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TintedClockLayer
import org.elnix.dragonlauncher.base.icons.TintedIconLayer
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.util.ImageUtils.tintedWith
import org.elnix.dragonlauncher.database.AppDatabase
import org.elnix.dragonlauncher.icons.compat.AdaptiveIconDrawableCompat
import org.elnix.dragonlauncher.icons.compat.toLauncherIcon
import org.elnix.dragonlauncher.icons.loaders.AppFilterIconPackInstaller
import org.elnix.dragonlauncher.icons.loaders.GrayscaleMapIconPackInstaller
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel
import org.elnix.dragonlauncher.ktx.randomElementOrNull
import org.elnix.dragonlauncher.logging.ICONS_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import kotlin.math.roundToInt


class IconPackManager(
    private val ctx: Context,
    private val appDatabase: AppDatabase,
) {
    fun getInstalledIconPacks(): Flow<List<IconPack>> {
        return appDatabase.iconDao().getInstalledIconPacks().map { packs ->
            packs.map { IconPack(it) }
        }
    }

    suspend fun getIconPack(packageName: String): IconPack? {
        return withContext(Dispatchers.IO) {
            appDatabase.iconDao().getIconPack(packageName)?.let {
                IconPack(it)
            }
        }
    }

    private var updateIconPacksMutex = Mutex()
    suspend fun updateIconPacks(forceReinstall: Boolean = false): Boolean {
        var iconsHaveBeenUpdated = false
        updateIconPacksMutex.lock()
        val installers = listOf(
            AppFilterIconPackInstaller(ctx, appDatabase),
            GrayscaleMapIconPackInstaller(ctx, appDatabase),
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

    suspend fun getIcon(
        iconPack: String,
        packageName: String,
        activityName: String?,
        allowThemed: Boolean = true
    ): LauncherIcon? = withContext(Dispatchers.IO) {
        val res = try {
            ctx.packageManager.getResourcesForApplication(iconPack)
        } catch (e: PackageManager.NameNotFoundException) {
            logE(ICONS_TAG, e) { "Icon pack package $iconPack not found!" }
            return@withContext null
        }
        val activity = activityName?.let { ComponentName(packageName, it) }?.shortClassName
        val iconDao = appDatabase.iconDao()
        val icon = iconDao.getIcon(packageName, activity, iconPack)?.let { IconPackAppIcon(it) }
            ?: return@withContext null

        when (icon) {
            is CalendarIcon -> {
                return@withContext getIconPackCalendarIcon(icon, res, allowThemed)
            }

            is AppIcon -> {
                return@withContext getIconPackStaticIcon(icon, res, allowThemed)
            }

            is ClockIcon -> {
                return@withContext getIconPackClockIcon(icon, res, allowThemed)
            }
        }
    }

    suspend fun getIcon(
        iconPack: String,
        icon: IconPackAppIcon,
        allowThemed: Boolean,
    ): LauncherIcon? = withContext(Dispatchers.IO) {
        val res = try {
            ctx.packageManager.getResourcesForApplication(iconPack)
        } catch (e: PackageManager.NameNotFoundException) {
            logE(ICONS_TAG, e) { "Icon pack package $iconPack not found!" }
            return@withContext null
        }
        when (icon) {
            is CalendarIcon -> {
                return@withContext getIconPackCalendarIcon(icon, res, allowThemed)
            }

            is AppIcon -> {
                return@withContext getIconPackStaticIcon(icon, res, allowThemed)
            }

            is ClockIcon -> {
                return@withContext getIconPackClockIcon(icon, res, allowThemed)
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    suspend fun generateIcon(
        ctx: Context,
        iconPack: String,
        baseIcon: Drawable,
        tint: Int?,
        size: Int
    ): LauncherIcon? = withContext(Dispatchers.IO) {
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
        outBounds = Rect(
            (bitmap.width * (1 - scale) * 0.5).roundToInt(),
            (bitmap.height * (1 - scale) * 0.5).roundToInt(),
            (bitmap.width - bitmap.width * (1 - scale) * 0.5).roundToInt(),
            (bitmap.height - bitmap.height * (1 - scale) * 0.5).roundToInt()
        )
        canvas.drawBitmap(icon, inBounds, outBounds, paint)

        val pm = ctx.packageManager
        val res = try {
            pm.getResourcesForApplication(iconPack)
        } catch (_: Resources.NotFoundException) {
            return@withContext null
        } catch (_: PackageManager.NameNotFoundException) {
            return@withContext null
        }

        if (mask != null) {
            res.getIdentifier(mask, "drawable", iconPack).takeIf { it != 0 }?.let {
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                val maskDrawable = try {
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
                val maskDrawable = try {
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
                val maskDrawable = try {
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
            foregroundLayer = StaticIconLayer(
                icon = bitmap.tintedWith(tint).toDrawable(ctx.resources),
                scale = 1f,
            ),
            backgroundLayer = TransparentLayer
        )
    }

    suspend fun getAllIconPackIcons(componentName: ComponentName): List<IconPackAppIcon> {
        val iconDao = appDatabase.iconDao()
        return iconDao.getIconsFromAllPacks(componentName.packageName, componentName.shortClassName)
            .mapNotNull { IconPackAppIcon(it) }
    }

    private suspend fun getIconBack(iconPack: String): String? {
        val iconDao = appDatabase.iconDao()
        val iconbacks = iconDao.getIconBacks(iconPack)
        return iconbacks.randomElementOrNull()
    }

    private suspend fun getIconUpon(iconPack: String): String? {
        val iconDao = appDatabase.iconDao()
        val iconupons = iconDao.getIconUpons(iconPack)
        return iconupons.randomElementOrNull()
    }

    private suspend fun getIconMask(iconPack: String): String? {
        val iconDao = appDatabase.iconDao()
        val iconmasks = iconDao.getIconMasks(iconPack)
        return iconmasks.randomElementOrNull()
    }

    private suspend fun getPackScale(iconPack: String): Float {
        val iconDao = appDatabase.iconDao()
        return iconDao.getScale(iconPack) ?: 1f
    }

    @SuppressLint("DiscouragedApi")
    private fun getIconPackStaticIcon(
        icon: AppIcon,
        resources: Resources,
        allowThemed: Boolean,
    ): LauncherIcon? {
        val resId =
            resources.getIdentifier(icon.drawable, "drawable", icon.iconPack).takeIf { it != 0 }
                ?: return null

        val adaptiveIconCompat = AdaptiveIconDrawableCompat.from(resources, resId)
        if (adaptiveIconCompat != null) {
            return adaptiveIconCompat.toLauncherIcon(themed = allowThemed && icon.themed)
        }
        val drawable = try {
            ResourcesCompat.getDrawable(resources, resId, ctx.theme) ?: return null
        } catch (_: Resources.NotFoundException) {
            return null
        }
        val themed = icon.themed && allowThemed
        return when {
            themed && drawable is AdaptiveIconDrawable -> {
                if (isAtLeastApiLevel(33) && drawable.monochrome != null) {
                    StaticLauncherIcon(
                        foregroundLayer = TintedIconLayer(
                            icon = drawable.monochrome!!,
                            scale = 1.5f,
                        ),
                        backgroundLayer = ColorLayer(),
                    )
                } else {
                    StaticLauncherIcon(
                        foregroundLayer = TintedIconLayer(
                            icon = drawable.foreground,
                            scale = 1.5f,
                        ),
                        backgroundLayer = ColorLayer(),
                    )
                }
            }

            themed -> {
                StaticLauncherIcon(
                    foregroundLayer = TintedIconLayer(
                        icon = drawable,
                        scale = 0.65f,
                    ),
                    backgroundLayer = ColorLayer(),
                )
            }

            drawable is AdaptiveIconDrawable -> {
                StaticLauncherIcon(
                    foregroundLayer = drawable.foreground?.let {
                        StaticIconLayer(
                            icon = it,
                            scale = 1.5f,
                        )
                    } ?: TransparentLayer,
                    backgroundLayer = drawable.background?.let {
                        StaticIconLayer(
                            icon = it,
                            scale = 1.5f,
                        )
                    } ?: TransparentLayer,
                )
            }

            else -> {
                StaticLauncherIcon(
                    foregroundLayer = StaticIconLayer(
                        icon = drawable,
                        scale = 1f
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
        allowThemed: Boolean,
    ): LauncherIcon? {
        val drawableIds = icon.drawables.map {
            val id = resources.getIdentifier(it, "drawable", icon.iconPack)
            if (id == 0) return null
            id
        }.toIntArray()


        if (icon.themed && allowThemed) {
            return ThemedDynamicCalendarIcon(
                resources = resources,
                resourceIds = drawableIds,
            )
        }
        return DynamicCalendarIcon(
            resources = resources,
            resourceIds = drawableIds,
        )
    }

    @SuppressLint("DiscouragedApi")
    private fun getIconPackClockIcon(
        icon: ClockIcon,
        resources: Resources,
        allowThemed: Boolean,
    ): LauncherIcon? {
        val drawableId = try {
            resources.getIdentifier(icon.drawable, "drawable", icon.iconPack).takeIf { it != 0 }
                ?: return null
        } catch (_: Resources.NotFoundException) {
            return null
        }
        val adaptiveIconCompat = AdaptiveIconDrawableCompat.from(resources, drawableId)
        if (adaptiveIconCompat != null) {
            return adaptiveIconCompat.toLauncherIcon(icon.themed && allowThemed, icon.config)
        }
        val drawable = try {
            ResourcesCompat.getDrawable(resources, drawableId, null)
        } catch (_: Resources.NotFoundException) {
            null
        } ?: return null

        val background = (drawable as? AdaptiveIconDrawable)?.background
        val foreground = (drawable as? AdaptiveIconDrawable)?.foreground ?: drawable

        if (foreground !is LayerDrawable) return null

        val layers = (0 until foreground.numberOfLayers).map {
            val drw = foreground.getDrawable(it)
            ClockSublayer(
                drawable = drw,
                role = when (it) {
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
                    foregroundLayer = TintedClockLayer(
                        defaultHour = icon.config.defaultHour,
                        defaultMinute = icon.config.defaultMinute,
                        defaultSecond = icon.config.defaultSecond,
                        sublayers = layers,
                        scale = 1.5f,
                    ),
                    backgroundLayer = ColorLayer(),
                )
            }

            themed -> {
                StaticLauncherIcon(
                    foregroundLayer = TintedClockLayer(
                        defaultHour = icon.config.defaultHour,
                        defaultMinute = icon.config.defaultMinute,
                        defaultSecond = icon.config.defaultSecond,
                        sublayers = layers,
                        scale = 1f,
                    ),
                    backgroundLayer = ColorLayer(),
                )
            }

            drawable is AdaptiveIconDrawable -> {
                StaticLauncherIcon(
                    foregroundLayer = ClockLayer(
                        defaultHour = icon.config.defaultHour,
                        defaultMinute = icon.config.defaultMinute,
                        defaultSecond = icon.config.defaultSecond,
                        sublayers = layers,
                        scale = 1.5f,
                    ),
                    backgroundLayer = StaticIconLayer(
                        icon = background!!,
                        scale = 1.5f
                    ),
                )
            }

            else -> {
                StaticLauncherIcon(
                    foregroundLayer = ClockLayer(
                        defaultHour = icon.config.defaultHour,
                        defaultMinute = icon.config.defaultMinute,
                        defaultSecond = icon.config.defaultSecond,
                        sublayers = layers,
                        scale = 1f,
                    ),
                    backgroundLayer = TransparentLayer,
                )
            }
        }
    }

    suspend fun searchIconPackIcon(query: String, iconPack: IconPack?): List<IconPackAppIcon> {
        val iconDao = appDatabase.iconDao()
        val drawableQuery = query.replace(" ", "_").lowercase()
        return iconDao.searchIconPackIcons(
            drawableQuery = "%$drawableQuery%",
            nameQuery = "%$query%",
            iconPack = iconPack?.packageName,
        ).mapNotNull {
            IconPackAppIcon(it)
        }.distinct()
    }
}

