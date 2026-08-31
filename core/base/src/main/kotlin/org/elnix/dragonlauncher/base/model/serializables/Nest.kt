@file:Suppress("NOTHING_TO_INLINE")

package org.elnix.dragonlauncher.base.model.serializables

import androidx.annotation.FloatRange
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.serializables.serializers.DpSerializer
import org.elnix.dragonlauncher.ktx.unless

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
    @Serializable(with = DpSerializer::class)
    val cancelZone: Dp? = null,
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
    val showAllShapes: Boolean? = null,
    /**
     * If true, all points in the nest are visible in the nest, regardless of points
     */
    @FloatRange(0.0, 5.0)
    val previewScaleFactor: Float? = null
) {
//    override fun toString(): String = "Nest(id = $id, contains ${intersectionShapes.size} shapes)"
//    override fun toString(): String = "Nest N°$id"

//    public fun scaledBy(scale: Float, defaultNest: Nest, defaultIntersectionShape: IntersectionShape): Nest =
//        this.copy(intersectionShapes = this.getInterSectionShapes(defaultNest).mapTo(mutableSetOf()) { it.scaledBy(scale, defaultIntersectionShape) })

    /**
     * I cannot name this one `getName` as it's a primal functions created by the data class
     */
    public inline fun nameOrId(): String =
        this.name ?: id.toString()

    public inline fun getInterSectionShapes(defaultNest: Nest, isDefaultEditing: Boolean): Set<IntersectionShape> =
        this.intersectionShapes ?: (defaultNest.intersectionShapes unless isDefaultEditing) ?: defaultIntersectionShapes

    public inline fun getCancelZone(defaultNest: Nest, isDefaultEditing: Boolean): Dp =
        this.cancelZone ?: (defaultNest.cancelZone unless isDefaultEditing) ?: defaultCancelZone

    public inline fun getShowAllPointsInCurrentNest(
        defaultNest: Nest,
        showAllPointsInCurrentNestSettings: Boolean,
        isDefaultEditing: Boolean
    ): Boolean =
        this.showAllPointsInCurrentNest ?: (defaultNest.showAllPointsInCurrentNest unless isDefaultEditing)
            ?: showAllPointsInCurrentNestSettings

    public inline fun getShowAllPointsInCurrentShape(
        defaultNest: Nest,
        showAllPointsInCurrentShapeSetting: Boolean,
        isDefaultEditing: Boolean
    ): Boolean =
        this.showAllPointsInCurrentShape ?: (defaultNest.showAllPointsInCurrentShape unless isDefaultEditing)
            ?: showAllPointsInCurrentShapeSetting

    public inline fun getShowCurrentShape(defaultNest: Nest, showCurrentShapeInNestSetting: Boolean, isDefaultEditing: Boolean): Boolean =
        this.showCurrentShape ?: (defaultNest.showCurrentShape unless isDefaultEditing) ?: showCurrentShapeInNestSetting

    public inline fun getShowAllShapes(defaultNest: Nest, showAllShapesSetting: Boolean, isDefaultEditing: Boolean): Boolean =
        this.showAllShapes ?: (defaultNest.showAllShapes unless isDefaultEditing) ?: showAllShapesSetting

    public inline fun getPreviewScaleFactor(defaultNest: Nest, isDefaultEditing: Boolean): Float =
        this.previewScaleFactor ?: (defaultNest.previewScaleFactor unless isDefaultEditing)
            ?: defaultPreviewScaledFactor

    @Suppress("ConstPropertyName")
    public companion object {
        public val defaultIntersectionShapes: Set<IntersectionShape> =
            setOf(
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

        public val defaultCancelZone: Dp = 50.dp

        public const val defaultPreviewScaledFactor: Float = 2f

        public val emptyNest: Nest = Nest()

        public inline val Nest.isDefault: Boolean
            get() =
                this.cancelZone == null &&
                    this.intersectionShapes == null &&
                    this.name == null &&
                    this.showAllPointsInCurrentShape == null &&
                    this.showAllPointsInCurrentNest == null &&
                    this.showCurrentShape == null &&
                    this.showAllShapes == null &&
                    this.previewScaleFactor == null

        public inline val Nest.isNotDefault: Boolean
            get() = !isDefault

        public object NestsJson : DragonJson<Set<Nest>>()

        public object DefaultNestJson : DragonJson<Nest>()
    }
}

public typealias Nests = Map<Int, Nest>
