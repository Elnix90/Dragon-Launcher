package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson


/**
 * New CircleNest system, where every bloc of circles is contained inside one of those*
 * This way, we can navigate across those nests, to achieve more actions, using the jump actions
 */
@Immutable
@Serializable
@SerialName("Nest")
public data class Nest(
    /**
     *  By default, the id 0 is the first nest that is available,
     *  I'll try to make the old system importable, to avoid breaking changes like empty actions circle
     */
    @SerialName("id")
    val id: Int = 0,

    /**
     * How far the user has to swipe to start actions triggering.
     * In the opposite; how far does the zone that triggers nothing extends
     */
    @SerialName("cancelZone")
    val cancelZone: Int = 150,

    /**
     * A set of one or more [IntersectionShape], each one of them belongs to the nest and
     */
    @SerialName("intersectionShapes")
    val intersectionShapes: Set<IntersectionShape> = defaultIntersectionShapes,
    /**
     * A custom name for the nest you can set for easier identification
     */
    @SerialName("name")
    val name: String? = null,

    /**
     * Haptic feedback, as default for the points in  the circle, separated from the point system
     */
    @SerialName("hapticFeedback")
    val haptic: CustomHapticFeedback? = null,

    /**
     * The nest radius, used to override the default nests radii, if set to null, it uses the default value, otherwise it picks this
     */
    @SerialName("nestRadius")
    val nestRadius: Int? = null,

    /**
     * If this nests displays it's circle, this is a per-nest setting
     */
    @SerialName("showCircle")
    val showCircle: Boolean? = null,

    /**
     * Same settings as in the global Appearance tab, but applied to this specific nest.
     * Names are self-explanatory
     */
    @SerialName("showAllActionsOnCurrentCircle")
    val showAllActionsOnCurrentCircle: Boolean? = null,

    @SerialName("showAllActionsOnCurrentNest")
    val showAllActionsOnCurrentNest: Boolean? = null,
) {
    override fun toString(): String = "Nest N°$id | contains ${intersectionShapes.size} shapes: "
//    override fun toString(): String = "Nest N°$id"

    public infix fun scaledBy(scale: Float): Nest = this.copy(intersectionShapes = this.intersectionShapes.mapTo(mutableSetOf()) { it scaledBy scale })

    public companion object {

        public val defaultIntersectionShapes: Set<IntersectionShape> = setOf(
            IntersectionShape(
                id = 0,
                shape = IconShape.Circle,
                size = 300f,
                centerOffset = Offset.Zero
            ),

            IntersectionShape(
                id = 1,
                shape = IconShape.Circle,
                size = 450f,
                centerOffset = Offset.Zero
            ),

            IntersectionShape(
                id = 1,
                shape = IconShape.Circle,
                size = 600f,
                centerOffset = Offset.Zero
            )
        )

        public object NestJson: DragonJson<List<Nest>>()
    }
}


public typealias Nests = Set<Nest>