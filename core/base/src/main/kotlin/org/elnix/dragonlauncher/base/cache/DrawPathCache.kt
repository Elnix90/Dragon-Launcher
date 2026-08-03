
package org.elnix.dragonlauncher.base.cache

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.model.serializables.IconShape

/**
 * Handles a cache where a icon shape and a size outputs a path, to avoid computing them everytime.
 *
 * I really don't want to handle the cache invalidation of this thing, so I'll keep it at 200 and call it a day.
 * Usually ppl don't usually have more than 200 points with sizes and shapes different, or they might be psychopaths
 */
public object DrawPathCache : DragonCache<Pair<IconShape, Size>, Path>(200)
