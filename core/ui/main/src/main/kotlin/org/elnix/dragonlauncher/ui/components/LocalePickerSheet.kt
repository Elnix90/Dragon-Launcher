package org.elnix.dragonlauncher.ui.components

import android.app.LocaleConfig
import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import java.util.Locale

private data class AppLocale(val locale: Locale, val name: String)

// yeeted from nsh04/Tomato

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LocalePickerSheet(onDismissRequest: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentLocales = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            ctx.getSystemService(LocaleManager::class.java).applicationLocales
        } else LocaleList.getEmptyLocaleList()
    }

    val supportedLocaleList: List<AppLocale>? = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            val supportedLocales = LocaleConfig(ctx).supportedLocales
            if (supportedLocales != null) {
                buildList {
                        for (i in 0 until supportedLocales.size()) {
                            val locale = supportedLocales.get(i)
                            add(
                                AppLocale(
                                    locale,
                                    locale.getDisplayName(locale).replaceFirstChar {
                                        it.uppercase()
                                    },
                                )
                            )
                        }
                    }
                    .sortedBy { it.name }
            } else null
        } else null
    }

    val supportedLocalesSize = supportedLocaleList?.size ?: 0

    DragonModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        if (supportedLocaleList != null) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(bottom = 60.dp),
                modifier = Modifier.heightIn(max = 600.dp).clip(shapes.large),
            ) {
                item {
                    SegmentedListItem(
                        onClick = {
                            scope
                                .launch {
                                    if (Build.VERSION.SDK_INT >= 33) {
                                        ctx
                                            .getSystemService(LocaleManager::class.java)
                                            .applicationLocales = LocaleList()
                                    }
                                    sheetState.hide()
                                }
                                .invokeOnCompletion { onDismissRequest() }
                        },
                        selected = currentLocales.isEmpty,
                        colors =  listItemColors(),
                        shapes = segmentedListItemShapes(0, 1),
                        content = { Text(text = stringResource(R.string.system_default)) },
                        trailingContent = {
                            if (currentLocales.isEmpty)
                                Icon(
                                    painter = painterResource(R.drawable.check_circle),
                                    contentDescription = null,
                                )
                        },
                    )
                }

                item { Spacer(12.dp) }

                itemsIndexed(
                    items = supportedLocaleList,
                    key = { _: Int, it: AppLocale -> it.name },
                ) { index, item ->
                    val selected = !currentLocales.isEmpty && item.locale == currentLocales.get(0)

                    SegmentedListItem(
                        onClick = {
                            scope
                                .launch {
                                    if (Build.VERSION.SDK_INT >= 33) {
                                        ctx
                                            .getSystemService(LocaleManager::class.java)
                                            .applicationLocales = LocaleList(item.locale)
                                    }
                                    sheetState.hide()
                                }
                                .invokeOnCompletion { onDismissRequest() }
                        },
                        selected = selected,
                        content = { Text(text = item.name) },
                        shapes = segmentedListItemShapes(index, supportedLocalesSize),
                        colors = listItemColors(),
                    )
                }
            }
        }
    }
}

@Composable
fun listItemColors(): ListItemColors {
    return ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun segmentedListItemShapes(
    index: Int,
    count: Int,
    singleElement: Boolean = count == 1,
): ListItemShapes =
    ListItemDefaults.segmentedShapes(
        index,
        count,
        ListItemDefaults.shapes(
            shape = if (singleElement) shapes.large else shapes.extraSmall,
            selectedShape = shapes.extraLargeIncreased,
            pressedShape = shapes.extraLargeIncreased,
            focusedShape = shapes.large,
            hoveredShape = shapes.extraLarge,
            draggedShape = shapes.extraLargeIncreased,
        ),
    )

