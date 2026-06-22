package org.elnix.dragonlauncher.ui.helpers.workspace

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.AppCategory
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.common.utils.LifecycleUtils.waitASec
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.drawer.AppItemGrid
import org.elnix.dragonlauncher.ui.drawer.AppItemHorizontal
import kotlin.math.min


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppGrid(
    apps: List<Application>,
    fillMaxSize: Boolean = true,

    gridState: LazyGridState? = null,
    categoryGridState: LazyGridState? = null,
    listState: LazyListState? = null,

    paddingValues: PaddingValues = PaddingValues(),

    drawerViewModel: DrawerViewModel = activityViewModel(),

    // Multi select things
    isMultiSelectMode: Boolean = false,
    selectedPackages: List<String> = emptyList(),
    onEnterMultiSelect: ((Application) -> Unit)? = null,
    onToggleSelect: ((Application) -> Unit)? = null,

    onTopStateChange: ((Boolean) -> Unit)? = null,
    longPressPopup: Boolean,
    onClick: ((Application) -> Unit)?
) {
    var openedCategory by remember { mutableStateOf<AppCategory?>(null) }

    val useCategory by DrawerSettingsStore.useCategory.asState()
    val gridSize by DrawerSettingsStore.gridSize.asState()
    val categoryGridCells by DrawerSettingsStore.categoryGridCells.asState()
    val iconsSpacingVertical by DrawerSettingsStore.iconsSpacingVertical.asState()
    val iconsSpacingHorizontal by DrawerSettingsStore.iconsSpacingHorizontal.asState()

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

                        Crossfade(isLoading) { showLoadingIcon ->
                            if (showLoadingIcon) {
                                CircularProgressIndicator()
                                LaunchedEffect(Unit) {
                                    waitASec()
                                    isLoading = false
                                }
                            } else {
                                DragonIconButton(
                                    icon = R.drawable.refresh,
                                    contentDescription = stringResource(R.string.reload_apps)
                                ) {
                                    drawerViewModel.reloadApps()
                                    isLoading = true
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
                columns = GridCells.Fixed(categoryGridCells),
                modifier = modifier,
                state = categoryGridState ?: rememberLazyGridState(),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(iconsSpacingVertical),
                horizontalArrangement = Arrangement.spacedBy(iconsSpacingHorizontal)
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
                verticalArrangement = Arrangement.spacedBy(iconsSpacingVertical),
            ) {
                items(visibleApps, key = { it.key.cacheKey }) { app ->
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
                verticalArrangement = Arrangement.spacedBy(iconsSpacingVertical),
                horizontalArrangement = Arrangement.spacedBy(iconsSpacingHorizontal)
            ) {
                items(visibleApps, key = { it.key.cacheKey }) { app ->
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
    apps: List<Application>,
    modifier: Modifier = Modifier,
    longPressPopup: Boolean,
    onClick: ((Application) -> Unit)?,
    onOpenCategory: () -> Unit
) {
    val categoryGridCells by DrawerSettingsStore.categoryGridCells.asState()
    val showCategoryName by DrawerSettingsStore.showCategoryName.asState()

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
                gridCells = categoryGridCells,
            )
        }

        if (showCategoryName) {
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
    apps: List<Application>,
    gridCells: Int,
    modifier: Modifier = Modifier,
    onLongClick: ((Application) -> Unit)? = null,
    longPressPopup: Boolean,
    onClick: ((Application) -> Unit)?,
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
                                    painter = painterResource(R.drawable.more_horiz),
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