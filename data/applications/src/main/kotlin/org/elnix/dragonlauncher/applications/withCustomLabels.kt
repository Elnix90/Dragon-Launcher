package org.elnix.dragonlauncher.applications

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager

fun Flow<ImmutableList<Application>>.withCustomLabels(
    appOverridesManager: AppOverridesManager
): Flow<ImmutableList<Application>> = channelFlow {
    this@withCustomLabels.collectLatest { items ->
        appOverridesManager.appOverrideState.collectLatest { state ->
            val labels = state.appOverrides
            send(items.map { item ->
                    val customLabel = labels[item.key]?.customName
                    if (customLabel != null) {
                        item.overrideLabel(customLabel)
                    } else {
                        item
                    }
            }.toImmutableList())
        }
    }
}