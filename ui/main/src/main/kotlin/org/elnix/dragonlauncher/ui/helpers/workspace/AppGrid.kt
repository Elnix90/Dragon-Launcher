package org.elnix.dragonlauncher.ui.helpers.workspace

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.models.AppCategory
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.compositionslocals.LocalDrawerSettings
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.drawer.AppItemGrid
import org.elnix.dragonlauncher.ui.drawer.AppItemHorizontal
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

@Immutable
private data class MutableCategory(
    val categoryName: String,
    val apps: List<Application>
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppGrid(
    apps: List<Application>,
    fillMaxSize: Boolean = true,
    gridState: LazyGridState? = null,
    categoryGridState: LazyGridState? = null,
    listState: LazyListState? = null,
    paddingValues: PaddingValues = PaddingValues(),
    // Multi select things
    isMultiSelectMode: Boolean = false,
    selectedPackages: List<Application> = emptyList(),
    onEnterMultiSelect: ((Application) -> Unit)? = null,
    onToggleSelect: ((Application) -> Unit)? = null,
    onReload: (() -> Unit)? = null,
    onTopStateChange: ((Boolean) -> Unit)? = null,
    longPressPopup: Boolean,
    onClick: ((Application) -> Unit)?
) {
    val drawerSettings = LocalDrawerSettings.current
    val useCategory = drawerSettings.useCategory
    val gridSize = drawerSettings.gridSize
    val iconsSpacingVertical = drawerSettings.iconsSpacingVertical
    val iconsSpacingHorizontal = drawerSettings.iconsSpacingHorizontal

    var openedCategory by remember { mutableStateOf<String?>(null) }

    val visibleApps by remember(apps) {
        derivedStateOf {
            if (useCategory) {
                apps.filter { openedCategory?.let { cat -> cat == it.effectiveCategory } ?: true }
            } else {
                apps
            }
        }
    }

    BackHandler(openedCategory != null) {
        openedCategory = null
    }

    val modifier = if (fillMaxSize) Modifier.fillMaxSize() else Modifier

    val isAtTop by remember {
        derivedStateOf {
            when {
                gridSize == 1 ->
                    listState?.firstVisibleItemIndex == 0 &&
                        listState.firstVisibleItemScrollOffset == 0

                useCategory && openedCategory == null && !isMultiSelectMode ->
                    categoryGridState?.firstVisibleItemIndex == 0 &&
                        categoryGridState.firstVisibleItemScrollOffset == 0

                else ->
                    gridState?.firstVisibleItemIndex == 0 &&
                        gridState.firstVisibleItemScrollOffset == 0
            }
        }
    }

    LaunchedEffect(isAtTop) {
        onTopStateChange?.invoke(isAtTop)
    }

    when {
        visibleApps.isEmpty() -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = paddingValues,
                state = listState ?: rememberLazyListState()
            ) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.no_apps),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (onReload != null) {
                            var isLoading by remember { mutableStateOf(false) }

                            Crossfade(isLoading) { showLoadingIcon ->
                                if (showLoadingIcon) {
                                    LoadingIndicator()
                                    LaunchedEffect(Unit) {
                                        delay(1.seconds)
                                        isLoading = false
                                    }
                                } else {
                                    DragonIconButton(
                                        icon = R.drawable.refresh,
                                        contentDescription = R.string.reload_apps
                                    ) {
                                        onReload()
                                        isLoading = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Can't use categories with multi-select mode cause it's too annoying to implement
        useCategory && openedCategory == null && !isMultiSelectMode -> {
            val ctx = LocalContext.current
            val scope = rememberCoroutineScope()
            val disabledSystemCategories = drawerSettings.disabledSystemCategories
            val categoryOrder = drawerSettings.categoryOrder

            // That's shitty code, and it should move to a viewmodel, but I don't care about the categories anyway

            val allCategoryNames =
                remember(visibleApps, disabledSystemCategories, categoryOrder) {
                    val systemCategories =
                        AppCategory.entries
                            .filter { it.name !in disabledSystemCategories }
                            .map { it.name }

                    val customCategories =
                        visibleApps
                            .mapNotNull { it.categoryOverride }
                            .distinct()

                    val allCategories = customCategories + systemCategories

                    if (categoryOrder.isNotEmpty()) {
                        allCategories.sortedBy { name ->
                            val idx = categoryOrder.indexOf(name)
                            if (idx >= 0) idx else Int.MAX_VALUE
                        }
                    } else {
                        allCategories
                    }
                }

            val mutableCategoryNames: SnapshotStateList<MutableCategory> =
                remember(allCategoryNames) {
                    mutableStateListOf<MutableCategory>().apply {
                        allCategoryNames.forEach { categoryName ->
                            val apps = visibleApps.filter { it.effectiveCategory == categoryName }
                            if (apps.isNotEmpty()) {
                                add(
                                    MutableCategory(
                                        categoryName = categoryName,
                                        apps = apps
                                    )
                                )
                            }
                        }
                    }
                }

            fun saveOrder() {
                scope.launch {
                    DrawerSettingsStore.categoryOrder.set(ctx, mutableCategoryNames.map { it.categoryName })
                }
            }

            val gridState = categoryGridState ?: rememberLazyGridState()
            val reorderState =
                rememberReorderableLazyGridState(
                    lazyGridState = gridState,
                    onMove = { from, to ->
                        mutableCategoryNames.apply {
                            add(to.index, removeAt(from.index))
                        }
                    }
                )

            LazyVerticalGrid(
                columns = GridCells.Fixed(drawerSettings.categoryCells),
                modifier = modifier,
                state = gridState,
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(iconsSpacingVertical),
                horizontalArrangement = Arrangement.spacedBy(iconsSpacingHorizontal)
            ) {
                items(
                    items = mutableCategoryNames,
                    key = { it.categoryName }
                ) { category ->
                    ReorderableItem(state = reorderState, key = category.categoryName) {
                        CategoryGrid(
                            categoryName = category.categoryName,
                            apps = category.apps,
                            modifier = Modifier.longPressDraggableHandle(
                                onDragStopped = ::saveOrder
                            ),
                            longPressPopup = longPressPopup,
                            onClick = onClick
                        ) {
                            openedCategory = category.categoryName
                        }
                    }
                }
            }
        }

        gridSize == 1 -> {
            LazyColumn(
                modifier = modifier,
                state = listState ?: rememberLazyListState(),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(iconsSpacingVertical)
            ) {
                items(visibleApps, key = { it.key.cacheKey }) { app ->
                    AppItemHorizontal(
                        app = app,
                        selected = app in selectedPackages,
                        onLongClick =
                            if (onEnterMultiSelect != null && onToggleSelect != null) {
                                {
                                    if (!isMultiSelectMode) {
                                        onEnterMultiSelect(app)
                                    } else {
                                        onToggleSelect(app)
                                    }
                                }
                            } else {
                                null
                            },
                        longPressPopup = longPressPopup,
                        onClick = {
                            if (isMultiSelectMode && onToggleSelect != null) {
                                onToggleSelect(app)
                            } else {
                                onClick?.invoke(app)
                            }
                        }
                    )
                }
            }
        }

        else -> {
            LazyVerticalGrid(
                modifier = modifier,
                state = gridState ?: rememberLazyGridState(),
                columns = GridCells.Fixed(gridSize),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(iconsSpacingVertical),
                horizontalArrangement = Arrangement.spacedBy(iconsSpacingHorizontal)
            ) {
                items(items = visibleApps, key = { it.key.cacheKey }) { app ->
                    AppItemGrid(
                        app = app,
                        selected = app in selectedPackages,
                        onLongClick =
                            if (onEnterMultiSelect != null && onToggleSelect != null) {
                                {
                                    if (!isMultiSelectMode) {
                                        onEnterMultiSelect(app)
                                    } else {
                                        onToggleSelect(app)
                                    }
                                }
                            } else {
                                null
                            },
                        longPressPopup = longPressPopup,
                        onClick = {
                            if (isMultiSelectMode && onToggleSelect != null) {
                                onToggleSelect(app)
                            } else {
                                onClick?.invoke(app)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    categoryName: String,
    apps: List<Application>,
    modifier: Modifier,
    longPressPopup: Boolean,
    onClick: ((Application) -> Unit)?,
    onOpenCategory: () -> Unit
) {
    val showCategoryName by DrawerSettingsStore.showCategoryName.asState()

    val drawerSettings = LocalDrawerSettings.current
    val gridCells = drawerSettings.categoryGridCells

    CompositionLocalProvider(
        LocalDrawerSettings provides
            drawerSettings.copy(
                iconSize = drawerSettings.iconSize / gridCells,
                showAppLabelsInDrawer = false
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier =
                    modifier
                        .aspectRatio(1f)
                        .padding(10.dp)
            ) {
                var appIndex = 0

                val appNumber = apps.size
                val maxAppNumber = gridCells * gridCells - 1
                val sanitizedAppNumber = min(appNumber, maxAppNumber)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(onClick = onOpenCategory)
                        .background(drawerSettings.categoryColor)
                ) {
                    repeat(gridCells) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(gridCells) {
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (appIndex < sanitizedAppNumber) {
                                        val app = apps[appIndex]

                                        AppItemGrid(
                                            app = app,
                                            selected = false,
                                            onLongClick = null,
                                            longPressPopup = longPressPopup,
                                            onClick = { onClick?.invoke(app) }
                                        )
                                    } else if (appIndex == maxAppNumber) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_horiz),
                                                contentDescription = "More",
                                                tint = contentColorFor(drawerSettings.categoryColor)
                                            )
                                        }
                                    }
                                }
                                appIndex++
                            }
                        }
                    }
                }
            }

            if (showCategoryName) {
                Text(
                    text = categoryName,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// @OptIn(ExperimentalMaterial3ExpressiveApi::class)
// @Composable
// fun WithFakeLoadingAnimation(
//    loadingDurationMillis: Long = 500L,
//    onClick: () -> Unit,
//    content: @Composable () -> Unit
// ) {
//    val isLoading by remember { mutableStateOf(false) }
//
//    Crossfade(isLoading) { showLoadingIcon ->
//        if (showLoadingIcon) {
//            LoadingIndicator()
//            LaunchedEffect(Unit) {
//                delay(loadingDurationMillis)
//            }
//        } else {
//            content()
//        }
//    }
// }
