package org.elnix.dragonlauncher.icons

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.PointApp
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.AdaptifiedLegacyIcon
import org.elnix.dragonlauncher.base.model.serializables.CacheKey
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon
import org.elnix.dragonlauncher.base.model.serializables.CustomIconPackIcon
import org.elnix.dragonlauncher.base.model.serializables.CustomIconProperties
import org.elnix.dragonlauncher.base.model.serializables.CustomTextIcon
import org.elnix.dragonlauncher.base.model.serializables.DefaultPlaceholderIcon
import org.elnix.dragonlauncher.base.model.serializables.ForceThemedIcon
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.UnmodifiedSystemDefaultIcon
import org.elnix.dragonlauncher.colors.ColorService
import org.elnix.dragonlauncher.icons.providers.CalendarIconProvider
import org.elnix.dragonlauncher.icons.providers.CompatIconProvider
import org.elnix.dragonlauncher.icons.providers.CustomIconPackIconProvider
import org.elnix.dragonlauncher.icons.providers.CustomTextIconProvider
import org.elnix.dragonlauncher.icons.providers.DynamicClockIconProvider
import org.elnix.dragonlauncher.icons.providers.IconPackIconProvider
import org.elnix.dragonlauncher.icons.providers.IconProvider
import org.elnix.dragonlauncher.icons.providers.PlaceholderIconProvider
import org.elnix.dragonlauncher.icons.providers.PointIconProvider
import org.elnix.dragonlauncher.icons.providers.SystemIconProvider
import org.elnix.dragonlauncher.icons.providers.ThemedPlaceholderIconProvider
import org.elnix.dragonlauncher.icons.providers.getFirstIcon
import org.elnix.dragonlauncher.icons.transformations.ForceThemedIconTransformation
import org.elnix.dragonlauncher.icons.transformations.LauncherIconTransformation
import org.elnix.dragonlauncher.icons.transformations.LegacyToAdaptiveTransformation
import org.elnix.dragonlauncher.icons.transformations.transform
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel
import org.elnix.dragonlauncher.logging.ICONS_TAG
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.recents.PointsService
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import kotlin.reflect.KClass

private object PointIconCache : DragonCache<CacheKey, LauncherIcon>(200)

private object ActionIconCache : DragonCache<KClass<out Action>, LauncherIcon>(Action.actionsNumber)
private object DrawerIconCache : DragonCache<CacheKey, LauncherIcon>(200)


class IconService(
    val ctx: Context,
    private val iconPackManager: IconPackManager,
    private val iconSettingsRepository: IconSettingsRepository,
    private val appOverrideManager: AppOverridesManager,
    private val pointService: PointsService,
    private val colorService: ColorService
) {
    private val density = Density(ctx.resources.displayMetrics.density)

    private val appReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            requestIconPackListUpdate()
        }
    }

    val iconSettings = iconSettingsRepository.settings
    val extraColors = colorService.extraColors

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    private val _packTint = MutableStateFlow<Int?>(null)

    val packTint = _packTint.asStateFlow()

    val defaultPoint: Flow<Point> = pointService.defaultPoint

    private val maxIconSize = DrawerSettingsStore.iconSize.flow(ctx)

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

        scope.launch {
            iconSettingsRepository.settings.distinctUntilChanged().collectLatest { settings ->
                iconPacksUpdated.collectLatest {
                    val fallbackProvider = if (settings.themedIcons) {
                        ThemedPlaceholderIconProvider(ctx)
                    } else {
                        PlaceholderIconProvider(ctx)
                    }
                    val providers = mutableListOf<IconProvider>()

                    if (!settings.iconPack.isNullOrBlank()) {
                        val pack = iconPackManager.getIconPack(settings.iconPack)
                        if (pack != null) {
                            providers.add(
                                IconPackIconProvider(
                                    ctx = ctx,
                                    iconPack = pack,
                                    tint = settings.iconPackTint,
                                    iconPackManager = iconPackManager,
                                    allowThemed = settings.themedIcons,
                                )
                            )
                        } else {
                            logW(ICONS_TAG) { "Icon pack ${settings.iconPack} not found" }
                        }
                    }
                    providers.add(DynamicClockIconProvider(ctx, settings.themedIcons))
                    providers.add(CalendarIconProvider(ctx, settings.themedIcons))
                    if (!isAtLeastApiLevel(33)) {
                        providers.add(CompatIconProvider(ctx, settings.themedIcons))
                    }
                    providers.add(SystemIconProvider(settings.themedIcons))
                    providers.add(fallbackProvider)

                    val transformations = mutableListOf<LauncherIconTransformation>()

                    if (settings.adaptify) transformations.add(LegacyToAdaptiveTransformation())
                    if (settings.themedIcons && settings.forceThemed) transformations.add(
                        ForceThemedIconTransformation()
                    )

                    iconProviders.value = providers
                    this@IconService.transformations.value = transformations
                }
            }
        }
    }

    fun getRandomAppIcon() = DrawerIconCache.getRandom()

    fun getCustomAppIcon(application: Application): Flow<CustomIcon?> {
        return appOverrideManager.appOverrideState.map {
            it[application.key]?.customIcon
        }
    }

    fun reloadAppIcon(application: Application) {
        @Suppress("UnusedFlow")
        getAppIcon(application, true)
    }

    fun reloadAllAppIcons() {
        DrawerIconCache.evictAll()
    }

    fun incrementPointCacheSize() {
        PointIconCache.incrementCacheSize()
    }

    fun updateMaxCacheSize(newSize: Int) {
        PointIconCache.updateMaxCacheSize(newSize)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAppIcon(
        application: Application,
        reload: Boolean = false
    ): Flow<LauncherIcon?> {
        val customIcon = getCustomAppIcon(application)


        return customIcon.flatMapLatest {
            val iconSize = maxIconSize.first()
            val size = (iconSize.value * density.density).toInt()

            resolveCustomAppIcon(application, size, reload, it)
        }
    }

    fun reloadPointIcon(point: Point) {
        @Suppress("UnusedFlow")
        getPointIcon(point, true)
    }

    fun reloadAllPointIcons() {
        PointIconCache.evictAll()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPointIcon(
        point: Point,
        reload: Boolean = false
    ): Flow<LauncherIcon?> {
        return defaultPoint.flatMapLatest { defaultPoint ->
            val resolvedResolutionDp =
                point.resolution ?: defaultPoint.resolution
                ?: point.size ?: defaultPoint.size
                ?: Point.defaultSwipePointsValues.size!!


            // Convert dp to pixels and enforce a minimum touch-safe size.
            val size = (resolvedResolutionDp * density.density).toInt()

            resolveCustomPointIcon(point, size, reload)
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
                cacheKey = application.key,
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

            icon = provs.getFirstIcon(application, size)

            if (icon != null) {
                icon = icon.transform(transforms)
                DrawerIconCache.compute(cacheKey) { icon }
            }
            return@combine icon
        }
    }

    private fun resolveCustomPointIcon(
        point: Point,
        size: Int,
        reload: Boolean,
    ): Flow<LauncherIcon?> {
        return combine(iconProviders, transformations, extraColors) { providers, transformations, extraColors ->
            val customIcon = point.customIcon

            /**
             * Tries to find the icon in the point cache, and if not found, create a new one, by searching in the
             */
            var icon = if (!reload) {
                PointIconCache[point.key] /*?: run {
                    when (val action = point.action) {
                        is Action.LaunchApp -> {
                            val pointKey = CacheKey(action.packageName, action.profile.userHandle.hashCode())
                            val key = CacheKey(
                                cacheKey = pointKey,
                                customIconHashCode = customIcon.hashCode(),
                                providersHashCode = providers.hashCode(),
                                transformationsHashcode = transformations.hashCode()
                            )
                            DrawerIconCache[key]
                        }

                        is Action.LaunchShortcut -> {
                            val pointKey = CacheKey(action.packageName, 0)
                            val key = CacheKey(
                                cacheKey = pointKey,
                                customIconHashCode = customIcon.hashCode(),
                                providersHashCode = providers.hashCode(),
                                transformationsHashcode = transformations.hashCode()
                            )
                            DrawerIconCache[key]
                        }

                        else -> null
                    }
                }*/
            } else null

            if (icon != null) {
                return@combine icon
            }
            val pointApp = PointApp(point)


            val pointIconProvider = PointIconProvider(
                ctx = ctx,
                point = point,
                extrasColors = extraColors
            )
            providers.toMutableList().add(0, pointIconProvider)

            val provs = if (customIcon != null) getProviders(customIcon) + providers else providers
            val transforms = getTransformations(customIcon) ?: transformations

            icon = provs.getFirstIcon(pointApp, size)

            if (icon != null) {
                icon = icon.transform(transforms)
                PointIconCache.compute(point.key) { icon }
            }
            return@combine icon
        }
    }

    private fun getProviders(customIcon: CustomIcon?): List<IconProvider> {
        if (customIcon is UnmodifiedSystemDefaultIcon) {
            return listOf(
                SystemIconProvider(false)
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


    fun requestIconPackListUpdate() {
        scope.launch {
            iconPackManager.updateIconPacks().also {
                if (it) iconPacksUpdated.tryEmit(Unit)
            }
        }
    }

    fun reinstallAllIconPacks() {
        scope.launch {
            iconPackManager.updateIconPacks(forceReinstall = true)
            iconPacksUpdated.tryEmit(Unit)
        }
    }

    fun getInstalledIconPacks(): Flow<List<IconPack>> {
        return iconPackManager.getInstalledIconPacks()
    }

    suspend fun getCustomIconSuggestions(
        application: Application,
        size: Int
    ): List<CustomIconWithPreview> {
        val suggestions = mutableListOf<CustomIconWithPreview>()

        val rawIcon = iconProviders.first().getFirstIcon(application, size) ?: return emptyList()

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

        val iconPackIcons = iconPackManager.getAllIconPackIcons(
            application.componentName
        )

        providerOptions.addAll(
            iconPackIcons.map {
                val ent = it.toDatabaseEntity()
                CustomIconPackIcon(
                    iconPackPackage = it.iconPack,
                    type = ent.type,
                    drawable = ent.drawable,
                    extras = ent.extras,
                    allowThemed = it.themed,
                )
            }
        )
        transformationOptions.add(
            ForceThemedIcon()
        )

        providerOptions.add(DefaultPlaceholderIcon())

        suggestions.addAll(
            transformationOptions.map {
                val transformations = getTransformations(it) ?: defaultTransformations
                val providers = getProviders(it)

                val icon = providers.getFirstIcon(application, size) ?: rawIcon

                CustomIconWithPreview(
                    preview = icon.transform(transformations),
                    customIcon = it,
                )

            }
        )

        suggestions.addAll(
            providerOptions.mapNotNull {
                val providers = getProviders(it)

                val icon = providers.getFirstIcon(application, size) ?: return@mapNotNull null

                CustomIconWithPreview(
                    preview = icon.transform(defaultTransformations),
                    customIcon = it,
                )

            }
        )

        return suggestions
    }

    suspend fun getUncustomizedDefaultIcon(
        application: Application,
        size: Int
    ): CustomIconWithPreview? {
        val icon = iconProviders.first().getFirstIcon(application, size)
            ?.transform(transformations.first()) ?: return null
        return CustomIconWithPreview(
            customIcon = null,
            preview = icon
        )
    }

    suspend fun searchCustomIcons(query: String, iconPack: IconPack?): List<CustomIconWithPreview> {
        val transformations = this.transformations.first()
        return iconPackManager.searchIconPackIcon(query, iconPack).flatMap {
            val themedIcon = if (it.themed) {
                iconPackManager.getIcon(it.iconPack, it, true)
                    ?.transform(transformations)
            } else null
            val unthemedIcon = iconPackManager.getIcon(it.iconPack, it, false)
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

data class CustomIconWithPreview(
    val preview: LauncherIcon,
    val customIcon: CustomIcon?,
)