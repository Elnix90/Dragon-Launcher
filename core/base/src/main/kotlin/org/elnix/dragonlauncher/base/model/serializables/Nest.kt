@file:Suppress("NOTHING_TO_INLINE")

package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.ktx.takeIfNot


/**
 * New CircleNest system, where every bloc of circles is contained inside one of those*
 * This way, we can navigate across those nests, to achieve more actions, using the jump actions
 *
 * **Note** null values means they use the global settings. you can override any nest options in the ***NestEditScreen*** one by one,  per-nest
 */
@Immutable
@Serializable
@SerialName("Nest")
public data class Nest(
    /**
     *  By default, the id 0 is the first nest that is available,
     *  I'll try to make the old system importable, to avoid breaking changes like empty actions circle
     */
    val id: Int = 0,

    /**
     * How far the user has to swipe to start actions triggering.
     * In the opposite; how far does the zone that triggers nothing extends
     */
    val cancelZone: Int? = null,

    /**
     * A set of one or more [IntersectionShape], each one of them belongs to the nest and
     */
    val intersectionShapes: Set<IntersectionShape>? = null,
    /**
     * A custom name for the nest you can set for easier identification
     */
    val name: String? = null,

    /**
     * Same settings as in the global Appearance tab, but applied to this specific nest.
     * Names are self-explanatory
     */
    val showAllPointsInCurrentShape: Boolean? = null,

    /**
     * If true, all points in the nest are visible when
     */
    val showAllPointsInCurrentNest: Boolean? = null,


    /**
     * If true, the shape that hosts selected points will be shown
     */
    val showCurrentShape: Boolean? = null,


    /**
     * If true, all points in the nest are visible in the nest, regardless of points
     */
    val showAllShapes: Boolean? = null
) {
//    override fun toString(): String = "Nest(id = $id, contains ${intersectionShapes.size} shapes)"
//    override fun toString(): String = "Nest N°$id"

    // TODO
//    public infix fun scaledBy(scale: Float): Nest = this
    //this.copy(intersectionShapes = this.intersectionShapes.mapTo(mutableSetOf()) { it scaledBy scale })

    /**
     * I cannot name this one `getName` as it's a primal functions created by the data class
     */
    public inline fun nameOrId(): String =
        this.name ?: id.toString()

    public inline fun getInterSectionShapes(defaultNest: Nest, defaultEditing: Boolean = false): Set<IntersectionShape> =
        this.intersectionShapes ?: defaultNest.intersectionShapes.takeIfNot(defaultEditing) ?: defaultIntersectionShapes

    public inline fun getCancelZone(defaultNest: Nest, defaultEditing: Boolean = false): Int =
        this.cancelZone ?: defaultNest.cancelZone.takeIfNot(defaultEditing) ?: defaultCancelZone

    public inline fun getShowAllPointsInCurrentNest(
        defaultNest: Nest,
        showAllPointsInCurrentNestSettings: Boolean,
        defaultEditing: Boolean = false
    ): Boolean =
        this.showAllPointsInCurrentNest ?: defaultNest.showAllPointsInCurrentNest.takeIfNot(defaultEditing) ?: showAllPointsInCurrentNestSettings

    public inline fun getShowAllPointsInCurrentShape(
        defaultNest: Nest,
        showAllPointsInCurrentShapeSetting: Boolean,
        defaultEditing: Boolean = false
    ): Boolean =
        this.showAllPointsInCurrentShape ?: defaultNest.showAllPointsInCurrentShape.takeIfNot(defaultEditing) ?: showAllPointsInCurrentShapeSetting

    public inline fun getShowCurrentShape(defaultNest: Nest, showCurrentShapeInNestSetting: Boolean, defaultEditing: Boolean = false): Boolean =
        this.showCurrentShape ?: defaultNest.showCurrentShape.takeIfNot(defaultEditing) ?: showCurrentShapeInNestSetting

    public inline fun getShowAllShapes(defaultNest: Nest, showAllShapesSetting: Boolean, defaultEditing: Boolean = false): Boolean =
        this.showAllShapes ?: defaultNest.showAllShapes.takeIfNot(defaultEditing) ?: showAllShapesSetting

    @Suppress("ConstPropertyName")
    public companion object {

        public val defaultIntersectionShapes: Set<IntersectionShape> = setOf(
            IntersectionShape(
                id = 0
            ),

            IntersectionShape(
                id = 1,
                scale = 1.5f
            ),

            IntersectionShape(
                id = 2,
                scale = 2f
            )
        )

        public const val defaultCancelZone: Int = 50


        public val defaultNestValues: Nest = Nest(
            id = -1,
            cancelZone = defaultCancelZone,
            intersectionShapes = defaultIntersectionShapes
        )

        public val emptyNest: Nest = Nest()

        public inline val Nest.isDefault: Boolean
            get() = this.cancelZone == null &&
                    this.intersectionShapes == null &&
                    this.name == null &&
                    this.showAllPointsInCurrentShape == null &&
                    this.showAllPointsInCurrentNest == null &&
                    this.showCurrentShape == null &&
                    this.showAllShapes == null

        public inline val Nest.isNotDefault: Boolean
            get() = !isDefault

        public object NestsJson : DragonJson<Set<Nest>>()
        public object DefaultNestJson : DragonJson<Nest>()
    }
}


public typealias Nests = Map<Int, Nest>