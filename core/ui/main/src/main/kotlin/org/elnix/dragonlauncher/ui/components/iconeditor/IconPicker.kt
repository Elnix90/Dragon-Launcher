package org.elnix.dragonlauncher.ui.components.iconeditor

import android.content.pm.PackageManager
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.icons.CustomIconWithPreview
import org.elnix.dragonlauncher.icons.IconPack
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.models.IconPickerVM
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.components.ShapedLauncherIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun IconPicker(
    application: Application,
    iconsViewModel: IconsViewModel = activityViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onSelect: (CustomIcon?) -> Unit
) {
    val iconSize = 48.dp
    val iconSizePx = iconSize.px

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val viewModel: IconPickerVM =
        remember(application.key) { iconsViewModel.getIconPickerVM(application) }

    val suggestions by remember { viewModel.getIconSuggestions(iconSizePx.toInt()) }
        .collectAsState(emptyList())

    val defaultIcon by remember {
        viewModel.getDefaultIcon(iconSizePx.toInt())
    }.collectAsState(null)

    var query by remember { mutableStateOf("") }
    var filterIconPack by remember { mutableStateOf<IconPack?>(null) }
    val isSearching by viewModel.isSearchingIcons
    val iconResults by viewModel.iconSearchResults

    var showIconPackFilter by remember { mutableStateOf(false) }
    val installedIconPacks by viewModel.installedIconPacks.collectAsState(null)
    val packsInstalled = installedIconPacks?.isEmpty() == false

    val columns by DrawerSettingsStore.gridSize.asState()

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(columns),
        contentPadding = contentPadding,
    ) {
        if (packsInstalled) {
            item(span = { GridItemSpan(columns) }) {
                SearchBar(
                    windowInsets = WindowInsets(0.dp),
                    expanded = false,
                    onExpandedChange = {},
                    inputField = {
                        InputField(
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.search),
                                    contentDescription = null
                                )
                            },
                            onSearch = {},
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = {
                                Text(stringResource(R.string.select_icon))
                            },
                            query = query,
                            onQueryChange = {
                                query = it
                                scope.launch {
                                    viewModel.searchIcon(query, filterIconPack)
                                }
                            },
                        )
                    }
                ) {

                }
            }
        }

        if (query.isEmpty()) {
            if (defaultIcon != null) {
                item(span = { GridItemSpan(columns) }) {
                    Separator(stringResource(R.string.icon_picker_default_icon))
                }
                item {
                    IconPreview(item = defaultIcon, iconSize = iconSize, onClick = {
                        onSelect(null)
                    })
                }
            }

            if (suggestions.isNotEmpty()) {
                item(span = { GridItemSpan(columns) }) {
                    Separator(stringResource(R.string.icon_picker_suggestions))
                }
                items(suggestions) {
                    IconPreview(
                        it,
                        iconSize,
                        onClick = { onSelect(it.customIcon) }
                    )
                }
            }
        } else {
            item(span = { GridItemSpan(columns) }) {
                Button(
                    onClick = { showIconPackFilter = !showIconPackFilter },
                    modifier = Modifier
                        .wrapContentWidth(align = Alignment.CenterHorizontally)
                        .padding(16.dp),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                ) {
                    if (filterIconPack == null) {
                        Icon(
                            modifier = Modifier
                                .padding(end = ButtonDefaults.IconSpacing)
                                .size(ButtonDefaults.IconSize),
                            painter = painterResource(R.drawable.filter_alt),
                            contentDescription = null
                        )
                    } else {
                        val icon = remember(filterIconPack?.packageName) {
                            try {
                                filterIconPack?.packageName?.let { pkg ->
                                    context.packageManager.getApplicationIcon(pkg)
                                }
                            } catch (_: PackageManager.NameNotFoundException) {
                                null
                            }
                        }
                        AsyncImage(
                            modifier = Modifier
                                .padding(end = ButtonDefaults.IconSpacing)
                                .size(ButtonDefaults.IconSize),
                            model = icon,
                            contentDescription = null
                        )
                    }
                    DropdownMenuPopup(
                        expanded = showIconPackFilter,
                        onDismissRequest = { showIconPackFilter = false }
                    ) {
                        DropdownMenuGroup(
                            shapes = MenuDefaults.groupShapes()
                        ) {
                            DropdownMenuItem(
                                selected = filterIconPack == null,
                                shapes = MenuDefaults.itemShape(
                                    0,
                                    installedIconPacks?.size?.plus(1) ?: 1
                                ),
                                text = { Text(stringResource(id = R.string.icon_picker_filter_all_packs)) },
                                onClick = {
                                    showIconPackFilter = false
                                    filterIconPack = null
                                    scope.launch {
                                        viewModel.searchIcon(query, filterIconPack)
                                    }
                                },
                                selectedLeadingIcon = {
                                    Icon(painterResource(R.drawable.check), null)
                                }
                            )
                            installedIconPacks?.forEachIndexed { i, iconPack ->
                                DropdownMenuItem(
                                    selected = filterIconPack == iconPack,
                                    shapes = MenuDefaults.itemShape(
                                        i + 1,
                                        installedIconPacks!!.size + 1
                                    ),
                                    onClick = {
                                        showIconPackFilter = false
                                        filterIconPack = iconPack
                                        scope.launch {
                                            viewModel.searchIcon(query, filterIconPack)
                                        }
                                    },
                                    text = {
                                        Text(iconPack.name)
                                    },
                                    selectedLeadingIcon = {
                                        Icon(painterResource(R.drawable.check), null)
                                    },
                                )
                            }
                        }
                    }
                    Text(
                        text = filterIconPack?.name
                            ?: stringResource(id = R.string.icon_picker_filter_all_packs),
                        modifier = Modifier.animateContentSize()
                    )
                    Icon(
                        painterResource(R.drawable.arrow_drop_down),
                        modifier = Modifier
                            .padding(start = ButtonDefaults.IconSpacing)
                            .size(ButtonDefaults.IconSize),
                        contentDescription = null
                    )
                }
            }

            items(iconResults) {
                IconPreview(
                    it,
                    iconSize,
                    onClick = { onSelect(it.customIcon) }
                )
            }

            if (isSearching) {
                item(span = { GridItemSpan(columns) }) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(12.dp)
                                .size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
public fun IconPreview(
    item: CustomIconWithPreview?,
    iconSize: Dp,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        ShapedLauncherIcon(
            maxSize = iconSize,
            icon = { item?.preview },
            modifier = Modifier.clickable(onClick = onClick),
        )
    }
}

@Composable
public fun Separator(label: String) {
    Text(
        label,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier
            .padding(top = 16.dp, bottom = 8.dp)
            .fillMaxWidth()
    )
}