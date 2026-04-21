package org.elnix.dragonlauncher.common.serializables

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * New CircleNest system, where every bloc of circles is contained inside one of those*
 * This way, we can navigate across those nests, to achieve more actions, using the jump actions
 */
@Immutable
@Serializable
data class CircleNest(
    /**
     *  By default, the id 0 is the first nest that is available,
     *  I'll try to make the old system importable, to avoid breaking changes like empty actions circle
     */
    @SerialName("id") val id: Int = 0,
    /**
     * Holds the cancel zone (index -1), and the circle numbers for each drag distances
     * for all the circles in the nest (index positive integer)
     * the key is the circle number, made for allowing not ascending order drag distances
     * For the last one, the drag distance has no limit, it's not even counted
     */
    @SerialName("dragDistances") val dragDistances: Map<Int, Int> = mapOf(
        -1 to 150,
        0 to 300,
        1 to 450,
        2 to 600
    ),
    /**
     * A custom name for the nest you can set for easier identification
     */
    @SerialName("name") val name: String? = null,

    /**
     * Haptic feedback, as default for the points in  the circle, separated from the point system
     */
    @SerialName("hapticFeedback") val haptic: Map<Int, CustomHapticFeedbackSerializable> = emptyMap(),

    /**
     * How far you have to be close to the closest point to activate it. If set to 0, the value is infinite
     */
    @SerialName("minAngleActivation") val minAngleActivation: Map<Int, Int> = emptyMap(),


    /**
     * The nest radius, used to override the default nests radii, if set to null, it uses the default value, otherwise it picks this
     */
    @SerialName("nestRadius") val nestRadius: Int? = null,

    /**
     * If this nests displays it's circle, this is a per-nest setting
     */
    @SerialName("showCircle") val showCircle: Boolean? = null,

    /**
     * Same settings as in the global Appearance tab, but applied to this specific nest.
     * Names are self-explanatory
     */
    @SerialName("showAllActionsOnCurrentCircle") val showAllActionsOnCurrentCircle: Boolean? = null,

    @SerialName("showAllActionsOnCurrentNest") val showAllActionsOnCurrentNest: Boolean? = null,
) {
    override fun toString(): String {
        return "Nest N°$id | contains ${dragDistances.size} circles: "//\n${dragDistances.map { "\n${it.key} to ${it.value}" }}"
    }
}
