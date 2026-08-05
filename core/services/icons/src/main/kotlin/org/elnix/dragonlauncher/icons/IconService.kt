package org.elnix.dragonlauncher.icons

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import io.github.elnix90.logging.ICONS_TAG
import io.github.elnix90.logging.logW
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager
import org.elnix.dragonlauncher.appshortcuts.AppShortcutRepository
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.AdaptifiedLegacyIcon
import org.elnix.dragonlauncher.base.model.serializables.CacheKey
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon.Companion.getProperties
import org.elnix.dragonlauncher.base.model.serializables.CustomIconPackIcon
import org.elnix.dragonlauncher.base.model.serializables.CustomIconProperties
import org.elnix.dragonlauncher.base.model.serializables.CustomTextIcon
import org.elnix.dragonlauncher.base.model.serializables.DefaultPlaceholderIcon
import org.elnix.dragonlauncher.base.model.serializables.ForceThemedIcon
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.UnmodifiedSystemDefaultIcon
import org.elnix.dragonlauncher.colors.ColorService
import org.elnix.dragonlauncher.icons.providers.ActionIconProvider
import org.elnix.dragonlauncher.icons.providers.CalendarIconProvider
import org.elnix.dragonlauncher.icons.providers.CompatIconProvider
import org.elnix.dragonlauncher.icons.providers.CustomIconPackIconProvider
import org.elnix.dragonlauncher.icons.providers.CustomTextIconProvider
import org.elnix.dragonlauncher.icons.providers.DynamicClockIconProvider
import org.elnix.dragonlauncher.icons.providers.IconPackIconProvider
import org.elnix.dragonlauncher.icons.providers.IconProvider
import org.elnix.dragonlauncher.icons.providers.PlaceholderIconProvider
import org.elnix.dragonlauncher.icons.providers.ShortcutIconProvider
import org.elnix.dragonlauncher.icons.providers.SystemIconProvider
import org.elnix.dragonlauncher.icons.providers.ThemedPlaceholderIconProvider
import org.elnix.dragonlauncher.icons.providers.getFirstIcon
import org.elnix.dragonlauncher.icons.transformations.ForceThemedIconTransformation
import org.elnix.dragonlauncher.icons.transformations.LauncherIconTransformation
import org.elnix.dragonlauncher.icons.transformations.LegacyToAdaptiveTransformation
import org.elnix.dragonlauncher.icons.transformations.transform
import org.elnix.dragonlauncher.ktx.dp
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore

private object PointIconCache : DragonCache<CacheKey, LauncherIcon>(200)

private object ActionIconCache : DragonCache<CacheKey, LauncherIcon>(Action.actionsNumber)
private object ShortcutIconCache : DragonCache<CacheKey, LauncherIcon>(Action.actionsNumber)
private object DrawerIconCache : DragonCache<CacheKey, LauncherIcon>(200)


public class IconService internal constructor(
    private val ctx: Context,
    private val iconPackManager: IconPackManager,
    private val iconSettingsRepository: IconSettingsRepository,
    private val appRepository: AppRepository,
    private val shortcutRepository: AppShortcutRepository,
    private val appOverrideManager: AppOverridesManager,
    private val pointService: PointsService,
    private val colorService: ColorService
) {
    private val density = Density(ctx.dp)

    private val appReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            requestIconPackListUpdate()
        }
    }

    private val iconSettings = iconSettingsRepository.settings
    private val extraColors = colorService.extraColors

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    private val defaultPoint: SettingFlow<Point> = pointService.defaultPoint

    private val iconSize = DrawerSettingsStore.iconSize.flow(ctx)

    private val iconProviders: MutableStateFlow<List<IconProvider>> = MutableStateFlow(listOf())

    /**
     * Signal that installed icon packs have been updated. Force a reload of all icons.
     */
    private val iconPacksUpdated = MutableSharedFlow<Unit>(1)

    private val transformations: MutableStateFlow<List<LauncherIconTransformation>> =
        MutableStateFlow(
            listOf()
        )

    init {
        requestIconPackListUpdate()
        ctx.registerReceiver(appReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_MY_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        })

        iconPacksUpdated.tryEmit(Unit)


        /**
         * Icon Settings reactive update to keep providers up-to-date
         */
        scope.launch {
            combine(
                iconSettingsRepository.settings.distinctUntilChanged(),
                extraColors
            ) { settings, colors ->
                Pair(settings, colors)
            }.collectLatest { (settings, extraColors) ->
                iconPacksUpdated.collectLatest {

                    fun tint(): Int? = if (!settings.onlyTintIconPacks) settings.iconsTint else null

                    val providers = mutableListOf<IconProvider>()

                    // the most important provider, the first one, returns null to let the other providers compute
                    providers.add(
                        ActionIconProvider(
                            ctx = ctx,
                            extraColors = extraColors
                        )
                    )

                    providers.add(
                        ShortcutIconProvider(
                            ctx, shortcutRepository,
                            themed = settings.themedIcons,
                            tint = tint(),
                        )
                    )

                    if (!settings.iconPack.isNullOrBlank()) {
                        val pack = iconPackManager.getIconPack(settings.iconPack!!)
                        if (pack != null) {
                            providers.add(
                                IconPackIconProvider(
                                    ctx = ctx,
                                    appRepository = appRepository,
                                    iconPack = pack,
                                    tint = settings.iconsTint,
                                    iconPackManager = iconPackManager,
                                    allowThemed = settings.themedIcons,
                                )
                            )
                        } else {
                            logW(ICONS_TAG) { "Icon pack ${settings.iconPack} not found" }
                        }
                    }

                    providers.add(
                        DynamicClockIconProvider(
                            ctx = ctx,
                            themed = settings.themedIcons,
                            tint = tint(),
                        )
                    )

                    providers.add(
                        CalendarIconProvider(
                            ctx = ctx,
                            appRepository = appRepository,
                            themed = settings.themedIcons,
                            tint = tint(),
                        )
                    )
                    if (!isAtLeastApiLevel(33)) {
                        providers.add(
                            CompatIconProvider(
                                appRepository = appRepository,
                                ctx = ctx,
                                themed = settings.themedIcons,
                                tint = tint(),
                            )
                        )
                    }
                    providers.add(
                        SystemIconProvider(
                            appRepository = appRepository,
                            themedIcons = settings.themedIcons,
                            tint = tint(),
                        )
                    )

                    val fallbackProvider = if (settings.themedIcons) {
                        ThemedPlaceholderIconProvider(
                            appRepository = appRepository,
                            ctx = ctx
                        )
                    } else {
                        PlaceholderIconProvider(
                            ctx = ctx,
                            appRepository = appRepository
                        )
                    }
                    providers.add(fallbackProvider)

                    val transformations = mutableListOf<LauncherIconTransformation>()

                    if (settings.adaptify) transformations.add(LegacyToAdaptiveTransformation())
                    if (settings.themedIcons && settings.forceThemed) transformations.add(
                        ForceThemedIconTransformation()
                    )

                    this@IconService.iconProviders.value = providers
                    this@IconService.transformations.value = transformations
                }
            }
        }
    }

//    public fun getRandomAppIcon(): CacheKey? = DrawerIconCache.getRandom()

    public fun getCustomAppIcon(application: Application): Flow<CustomIcon?> {
        return appOverrideManager.appOverrides.flow.map {
            it[application.key]?.customIcon
        }
    }

    public fun reloadAppIcon(application: Application) {
        @Suppress("UnusedFlow")
        getAppIcon(application, true)
    }

    public fun reloadAllAppIcons() {
        DrawerIconCache.evictAll()
    }

    public fun incrementPointCacheSize() {
        PointIconCache.incrementCacheSize()
    }

    public fun updateMaxCacheSize(newSize: Int) {
        PointIconCache.updateMaxCacheSize(newSize)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    public fun getAppIcon(
        application: Application,
        reload: Boolean = false
    ): Flow<LauncherIcon?> {
        val customIcon = getCustomAppIcon(application)

        return customIcon.flatMapLatest { customIcon ->
            val iconSize = iconSize.first()
            val size = (iconSize.value * density.density).toInt()

            resolveCustomAppIcon(application, size, reload, customIcon)
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    public fun getActionIcon(
        action: Action,
        reload: Boolean = false
    ): Flow<LauncherIcon?> {

        return combine(iconSize, iconProviders, transformations) { iconSize, providers, transformations ->
            val size = (iconSize * density.density).value.toInt()

            val cacheKey = CacheKey(
                data = action::class,
                customIconHashCode = 0,
                providersHashCode = providers.hashCode(),
                transformationsHashcode = transformations.hashCode()
            )

            var icon = if (!reload) {
                ActionIconCache[cacheKey]
            } else null

            if (!reload && icon != null) {
                return@combine icon
            }

            icon = providers.getFirstIcon(action, size)

            if (icon != null) {
                icon = icon.transform(transformations)
                ActionIconCache.compute(cacheKey) { icon }
            }
            return@combine icon
        }
    }


    public fun reloadPointIcon(point: Point) {
        @Suppress("UnusedFlow")
        getPointIcon(point, true)
    }

    public fun reloadAllPointIcons() {
        PointIconCache.evictAll()
        pointService.recompose()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    public fun getPointIcon(
        point: Point,
        reload: Boolean = false
    ): Flow<LauncherIcon?> {
        return defaultPoint.flow.flatMapLatest { defaultPoint ->
            val size = point.getSize(defaultPoint, false) // TODO
            resolveCustomPointIcon(point, size, reload)
        }
    }


    public fun reloadShortcutIcon(shortcut: Action.LaunchShortcut) {
        @Suppress("UnusedFlow")
        getShortcutIcon(shortcut, true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    public fun getShortcutIcon(
        shortcut: Action.LaunchShortcut,
        reload: Boolean = false
    ): Flow<LauncherIcon?> {
        return combine(iconProviders, transformations) { providers, transformations ->

            val cacheKey = CacheKey(
                data = shortcut,
                customIconHashCode = 0,
                providersHashCode = providers.hashCode(),
                transformationsHashcode = transformations.hashCode()
            )

            var icon = if (!reload) {
                DrawerIconCache[cacheKey]
            } else null

            if (!reload && icon != null) {
                return@combine icon
            }

            icon = providers.getFirstIcon(shortcut, 56)

            if (icon != null) {
                icon = icon.transform(transformations)
                DrawerIconCache.compute(cacheKey) { icon }
            }
            return@combine icon
        }
    }

    private fun resolveCustomAppIcon(
        application: Application,
        size: Int,
        reload: Boolean,
        customIcon: CustomIcon?
    ): Flow<LauncherIcon?> {
        return combine(iconProviders, transformations) { providers, transformations ->

            val cacheKey = CacheKey(
                data = application.key,
                customIconHashCode = customIcon.hashCode(),
                providersHashCode = providers.hashCode(),
                transformationsHashcode = transformations.hashCode()
            )

            var icon = if (!reload) {
                DrawerIconCache[cacheKey]
            } else null

            if (!reload && icon != null) {
                return@combine icon
            }

            val provs = if (customIcon != null) getProviders(customIcon) + providers else providers
            val transforms = getTransformations(customIcon) ?: transformations

            icon = provs.getFirstIcon(Action.LaunchApp(application), size)

            if (icon != null) {
                icon = icon.transform(transforms)
                DrawerIconCache.compute(cacheKey) { icon }
            }
            return@combine icon
        }
    }

    private fun resolveCustomPointIcon(
        point: Point,
        size: Dp,
        reload: Boolean,
    ): Flow<LauncherIcon?> {
        return combine(iconProviders, transformations, extraColors) { providers, transformations, _ ->
            val customIcon = point.customIcon

            val cacheKey = CacheKey(
                data = point.key,
                customIconHashCode = customIcon.hashCode(),
                providersHashCode = providers.hashCode(),
                transformationsHashcode = transformations.hashCode()
            )

            /**
             * Tries to find the icon in the point cache, and if not found, create a new one, by searching in the
             */
            var icon = if (!reload) {
                PointIconCache[cacheKey] ?: run {
                    when (point.action) {
                        is Action.LaunchApp -> {
                            DrawerIconCache[cacheKey]
                        }


                        // TODO
                        is Action.LaunchShortcut -> {
                            ShortcutIconCache[cacheKey]
                        }

                        else -> null
                    }
                }
            } else null

            if (icon != null) {
                return@combine icon
            }

            val provs = if (customIcon != null) getProviders(customIcon) + providers else providers
            val transforms = getTransformations(customIcon) ?: transformations

            icon = provs.getFirstIcon(point.action, (size.value * density.density).toInt())

            if (icon != null) {
                icon = icon.transform(transforms)
                PointIconCache.compute(cacheKey) { icon }
            }
            return@combine icon
        }
    }

    private fun getProviders(customIcon: CustomIcon?): List<IconProvider> {
        if (customIcon is UnmodifiedSystemDefaultIcon) {
            return listOf(
                SystemIconProvider(
                    appRepository = appRepository,
                    themedIcons = false,
                    tint = customIcon.getProperties().tint?.toArgb()
                )
            )
        }
        if (customIcon is CustomIconPackIcon) {
            return listOf(
                CustomIconPackIconProvider(
                    customIcon,
                    iconPackManager
                )
            )
        }

        if (customIcon is DefaultPlaceholderIcon) {
            return iconProviders.value.lastOrNull()?.let { listOf(it) } ?: emptyList()
        }
        if (customIcon is CustomTextIcon) {
            return listOf(CustomTextIconProvider(customIcon))
        }
        return emptyList()
    }

    private fun getTransformations(customIcon: CustomIcon?): List<LauncherIconTransformation>? {
        customIcon ?: return null
        if (customIcon is AdaptifiedLegacyIcon) {
            return listOf(
                LegacyToAdaptiveTransformation(
                    foregroundScale = customIcon.fgScale,
                    backgroundColor = customIcon.bgColor
                )
            )
        }
        if (customIcon is ForceThemedIcon) {
            return listOf(
                ForceThemedIconTransformation()
            )
        }
        if (customIcon is UnmodifiedSystemDefaultIcon) {
            return emptyList()
        }
        return null
    }


    public fun requestIconPackListUpdate() {
        scope.launch {
            iconPackManager.updateIconPacks().also {
                if (it) iconPacksUpdated.tryEmit(Unit)
            }
        }
    }

    public fun reinstallAllIconPacks() {
        scope.launch {
            iconPackManager.updateIconPacks(forceReinstall = true)
            iconPacksUpdated.tryEmit(Unit)
        }
    }

    public fun getInstalledIconPacks(): Flow<List<IconPack>> {
        return iconPackManager.getInstalledIconPacks()
    }

    public suspend fun getCustomIconSuggestions(
        action: Action,
        size: Int
    ): List<CustomIconWithPreview> {
        val suggestions = mutableListOf<CustomIconWithPreview>()

        val rawIcon = iconProviders.first().getFirstIcon(action, size) ?: return emptyList()

        val defaultTransformations = transformations.first()

        val transformationOptions = mutableListOf<CustomIcon>()

        transformationOptions.add(UnmodifiedSystemDefaultIcon())

        if (rawIcon is StaticLauncherIcon && rawIcon.backgroundLayer is TransparentLayer) {
            // Legacy icons that simply fill the entire canvas
            transformationOptions.add(
                AdaptifiedLegacyIcon(
                    fgScale = 1f,
                    bgColor = 1
                )
            )
            // 48x48 with 5px padding used to be the default icon size for icons generated by
            // the Android Studio asset generator. Upscale these icons to remove that padding.

            transformationOptions.add(
                AdaptifiedLegacyIcon(
                    fgScale = 48f / 38f,
                    bgColor = 1
                )
            )

            // Android 7.1 round icons (48x48 circle with 1px padding)
            transformationOptions.add(
                AdaptifiedLegacyIcon(
                    fgScale = 48f / 44f,
                    bgColor = 1
                )
            )
            transformationOptions.add(
                AdaptifiedLegacyIcon(
                    fgScale = 0.7f,
                    bgColor = 0
                )
            )
            transformationOptions.add(
                AdaptifiedLegacyIcon(
                    fgScale = 0.7f,
                    bgColor = Color.White.toArgb(),
                )
            )
        }

        val providerOptions = mutableListOf<CustomIcon>()

        if (action is Action.LaunchApp) {
            appRepository.fromAction(action)?.let { app ->
                val iconPackIcons = iconPackManager.getAllIconPackIcons(app.componentName)

                providerOptions.addAll(
                    iconPackIcons.map {
                        val ent = it.toDatabaseEntity()
                        CustomIconPackIcon(
                            iconPackPackage = it.iconPack,
                            type = ent.type,
                            drawable = ent.drawable,
                            extras = ent.extras,
                            allowThemed = it.themed,
                            tint = it.tint
                        )
                    }
                )
            }
        }

        transformationOptions.add(
            ForceThemedIcon()
        )

        providerOptions.add(DefaultPlaceholderIcon())

        suggestions.addAll(
            transformationOptions.map {
                val transformations = getTransformations(it) ?: defaultTransformations
                val providers = getProviders(it)

                val icon = providers.getFirstIcon(action, size) ?: rawIcon

                CustomIconWithPreview(
                    preview = icon.transform(transformations),
                    customIcon = it,
                )

            }
        )

        suggestions.addAll(
            providerOptions.mapNotNull {
                val providers = getProviders(it)

                val icon = providers.getFirstIcon(action, size) ?: return@mapNotNull null

                CustomIconWithPreview(
                    preview = icon.transform(defaultTransformations),
                    customIcon = it,
                )

            }
        )

        return suggestions
    }

    public suspend fun getUncustomizedDefaultIcon(
        action: Action,
        size: Int
    ): CustomIconWithPreview? {
        val icon = iconProviders.first().getFirstIcon(action, size)
            ?.transform(transformations.first()) ?: return null
        return CustomIconWithPreview(
            customIcon = null,
            preview = icon
        )
    }

    public suspend fun searchCustomIcons(query: String, iconPack: IconPack?): List<CustomIconWithPreview> {
        val transformations = this.transformations.first()
        val tint = iconSettings.first().iconsTint
        return iconPackManager.searchIconPackIcon(query, iconPack).flatMap {
            val themedIcon = if (it.themed) {
                iconPackManager.getIcon(it.iconPack, it, tint, true)
                    ?.transform(transformations)
            } else null
            val unthemedIcon = iconPackManager.getIcon(it.iconPack, it, tint, false)
                ?.transform(transformations)

            buildList {
                val ent = it.toDatabaseEntity()
                if (unthemedIcon != null) {
                    add(
                        CustomIconWithPreview(
                            customIcon = CustomIconPackIcon(
                                iconPackPackage = it.iconPack,
                                type = ent.type,
                                drawable = ent.drawable,
                                extras = ent.extras,
                                allowThemed = false,
                                tint = tint,
                                properties = CustomIconProperties()
                            ),
                            preview = unthemedIcon
                        )
                    )
                }
                if (themedIcon != null) {
                    add(
                        CustomIconWithPreview(
                            customIcon = CustomIconPackIcon(
                                iconPackPackage = it.iconPack,
                                type = ent.type,
                                drawable = ent.drawable,
                                extras = ent.extras,
                                allowThemed = true,
                                tint = tint,
                                properties = CustomIconProperties()
                            ),
                            preview = themedIcon
                        )
                    )
                }
            }
        }
    }
}

public data class CustomIconWithPreview(
    val preview: LauncherIcon,
    val customIcon: CustomIcon?,
)