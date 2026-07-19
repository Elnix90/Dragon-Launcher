package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson


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
    val cancelZone: Int = defaultCancelZone,

    /**
     * A set of one or more [IntersectionShape], each one of them belongs to the nest and
     */
    val intersectionShapes: Set<IntersectionShape> = defaultIntersectionShapes,
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
    public infix fun scaledBy(scale: Float): Nest = this
        //this.copy(intersectionShapes = this.intersectionShapes.mapTo(mutableSetOf()) { it scaledBy scale })

    public fun nameOrId(): String = this.name ?: id.toString()

    @Suppress("ConstPropertyName")
    public companion object {

        public val defaultIntersectionShapes: Set<IntersectionShape> = setOf(
            IntersectionShape(
                id = 0
             ),

            IntersectionShape(
                id = 1,
                scale = 1.5f,
            ),

            IntersectionShape(
                id = 2,
                scale = 2f,
            )
        )


        public const val defaultCancelZone: Int = 50

        public object NestJson: DragonJson<Set<Nest>>()
    }
}


public typealias Nests = Map<Int, Nest>