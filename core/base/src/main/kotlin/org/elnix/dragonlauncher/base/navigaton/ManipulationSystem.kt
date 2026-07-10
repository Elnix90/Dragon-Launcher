package org.elnix.dragonlauncher.base.navigaton

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.ui.geometry.Offset
import org.elnix.dragonlauncher.ktx.applyTransformations
import org.elnix.dragonlauncher.ktx.undoTransformations

public class ManipulationSystem(
    public var center: Offset,
) {
    public val offset: Animatable<Offset, AnimationVector2D> = Animatable(Offset.Zero, Offset.VectorConverter)
    public val zoom: Animatable<Float, AnimationVector1D> = Animatable(1f)
    public val angle: Animatable<Float, AnimationVector1D> = Animatable(0f)


    /**
     * Normalize a [org.elnix.dragonlauncher.base.model.serializables.Point.offset]
     *
     * It should take a **non-transformed** offset in entry and returns its offset normalized around [Offset.Zero], effectively subtracting the center from it
     */
    public fun normalize(offset: Offset): Offset = offset - center

    /**
     * Undo normalization of a [org.elnix.dragonlauncher.base.model.serializables.Point.offset]
     * @returns the `transformed` [Offset] corresponding to the offset in local coordinates space, when transformations are applied
     */
    public fun undoNormalization(offset: Offset): Offset = offset + center


    /**
     * Transform a pointer position [Offset] into its coordinated, after applying [offset], [zoom] and [angle] transformations.
     * The resulted [Offset] is meant to be used within the [androidx.compose.ui.graphics.graphicsLayer] block in the Main drawing block
     */
    public fun transform(offset: Offset): Offset = offset.applyTransformations(
        zoom = this.zoom.value,
        offset = this.offset.value,
        angle = this.angle.value
    )

    /**
     * Undo transformation of the above [transform] function, basically applying the same calculation in the opposite direction.
     * It provides real screen [Offset] from a transformed [Offset]
     * It is uses in the [androidx.compose.ui.input.pointer.pointerInput] block in this file, to provide the real screen position of the computed point offset, when user want to snap point to the shapes
     *
     * May be removed in the future
     */
    public fun undoTransformation(offset: Offset): Offset = offset.undoTransformations(
        zoom = this.zoom.value,
        offset = this.offset.value,
        angle = this.angle.value
    )


    public fun undoBoth(offset: Offset): Offset = undoTransformation(undoNormalization(offset))
//        pointsService.computePointOffset(this).undoNormalization().undoTransformation()
}