package org.elnix.dragonlauncher.ui.helpers

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.AppCategory
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.common.utils.waitASec
import org.elnix.dragonlauncher.ui.base.compositionslocals.LocalAppItemSettings
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.drawer.AppItemGrid
import org.elnix.dragonlauncher.ui.drawer.AppItemHorizontal
import kotlin.math.min


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppGrid(
    apps: List<AppModel>,
    fillMaxSize: Boolean = true,

    gridState: LazyGridState? = null,
    categoryGridState: LazyGridState? = null,
    listState: LazyListState? = null,

    paddingValues: PaddingValues = PaddingValues(),

    // Multi select things
    isMultiSelectMode: Boolean = false,
    selectedPackages: List<String> = emptyList(),
    onEnterMultiSelect: ((AppModel) -> Unit)? = null,
    onToggleSelect: ((AppModel) -> Unit)? = null,

    onTopStateChange: ((Boolean) -> Unit)? = null,
    onReload: (() -> Unit)? = null,
    longPressPopup: @Composable ((AppModel) -> Unit)?,
    onClick: ((AppModel) -> Unit)?
) {
    var openedCategory by remember { mutableStateOf<AppCategory?>(null) }

    val appItemSettings = LocalAppItemSettings.current


    val categorySettings = appItemSettings.categorySettings
    val useCategory = categorySettings.useCategory

    val gridSize = appItemSettings.gridSize

    val visibleApps by remember(apps) {
        derivedStateOf {
            // Only display the apps that belongs to the selected category, if enabled
            apps.filter {
                if (useCategory) openedCategory?.let { cat -> cat == it.category } ?: true
                else true
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
                appItemSettings.gridSize == 1 ->
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
                modifier = modifier
                    .fillMaxSize(),
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

                        var isLoading by remember { mutableStateOf(false) }

                        if (onReload != null) {
                            Crossfade(isLoading) { showLoadingIcon ->
                                if (showLoadingIcon) {
                                    CircularProgressIndicator()
                                    LaunchedEffect(Unit) {
                                        waitASec()
                                        isLoading = false
                                    }
                                } else {
                                    DragonIconButton(
                                        onClick = {
                                            onReload()
                                            isLoading = true
                                        }, imageVector = Icons.Default.Refresh,
                                        contentDescription = stringResource(R.string.reload_apps)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Can't use categories with multi-select mode cause it's too annoying to implement
        useCategory && openedCategory == null && !isMultiSelectMode -> {


            LazyVerticalGrid(
                columns = GridCells.Fixed(appItemSettings.gridSize),
                modifier = modifier,
                state = categoryGridState ?: rememberLazyGridState(),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(appItemSettings.iconSpacingVertical),
                horizontalArrangement = Arrangement.spacedBy(appItemSettings.iconSpacingHorizontal)
            ) {

                AppCategory.entries.forEach { category ->
                    val categoryApps = visibleApps.filter { it.category == category }

                    categoryApps
                        .takeIf { it.isNotEmpty() }
                        ?.let {
                            item {
                                CategoryGrid(
                                    category = category,
                                    apps = categoryApps,

                                    longPressPopup = longPressPopup,
                                    onClick = onClick,
                                ) {
                                    openedCategory = category
                                }
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
                verticalArrangement = Arrangement.spacedBy(appItemSettings.iconSpacingVertical),
            ) {
                items(visibleApps, key = { it.iconCacheKey.cacheKey }) { app ->
                    val selected = app.packageName in selectedPackages

                    AppItemHorizontal(
                        app = app,
                        selected = selected,
                        onLongClick = if (onEnterMultiSelect != null && onToggleSelect != null) { app ->
                            if (!isMultiSelectMode) {
                                onEnterMultiSelect(app)
                            } else {
                                onToggleSelect(app)
                            }
                        } else null,
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
                verticalArrangement = Arrangement.spacedBy(appItemSettings.iconSpacingVertical),
                horizontalArrangement = Arrangement.spacedBy(appItemSettings.iconSpacingHorizontal)
            ) {
                items(visibleApps, key = { it.iconCacheKey.cacheKey }) { app ->
                    val selected = app.packageName in selectedPackages

                    AppItemGrid(
                        app = app,
                        selected = selected,
                        onLongClick = if (onEnterMultiSelect != null && onToggleSelect != null) { app ->
                            if (!isMultiSelectMode) {
                                onEnterMultiSelect(app)
                            } else {
                                onToggleSelect(app)
                            }
                        } else null,
                        longPressPopup = longPressPopup,
                        onClick = {
                            if (isMultiSelectMode && onToggleSelect != null) {
                                onToggleSelect(app)
                            } else onClick?.invoke(app)
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun CategoryGrid(
    category: AppCategory,
    apps: List<AppModel>,
    modifier: Modifier = Modifier,
    longPressPopup: @Composable ((AppModel) -> Unit)?,
    onClick: ((AppModel) -> Unit)?,
    onOpenCategory: () -> Unit
) {
    val categorySettings = LocalAppItemSettings.current.categorySettings

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .shapedClickable { onOpenCategory() }
                .padding(10.dp)
        ) {
            AppDefinedGrid(
                apps = apps,
                longPressPopup = longPressPopup,
                onClick = onClick,
                gridCells = categorySettings.categoryGridCells,
            )
        }

        if (categorySettings.showCategoryName) {
            Text(
                text = category.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun AppDefinedGrid(
    apps: List<AppModel>,
    gridCells: Int,
    modifier: Modifier = Modifier,
    onLongClick: ((AppModel) -> Unit)? = null,
    longPressPopup: @Composable ((AppModel) -> Unit)?,
    onClick: ((AppModel) -> Unit)?,
) {
    var appIndex = 0

    val appNumber = apps.size
    val maxAppNumber = gridCells * gridCells - 1
    val sanitizedAppNumber = min(appNumber, maxAppNumber)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        repeat(gridCells) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                repeat(gridCells) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (appIndex < sanitizedAppNumber) {
                            AppItemGrid(
                                app = apps[appIndex],
                                selected = false,
                                onLongClick = onLongClick,
                                longPressPopup = longPressPopup,
                                onClick = onClick
                            )
                        } else if (appNumber > maxAppNumber) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "More",
                                    tint = MaterialTheme.colorScheme.onBackground,
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


//@OptIn(ExperimentalMaterial3ExpressiveApi::class)
//@Composable
//fun WithFakeLoadingAnimation(
//    loadingDurationMillis: Long = 500L,
//    onClick: () -> Unit,
//    content: @Composable () -> Unit
//) {
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
//}