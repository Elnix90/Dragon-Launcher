package org.elnix.dragonlauncher.material.shapes

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon

/**
 * Returns a [Path] for this [Morph].
 *
 * @param progress the [Morph]'s progress
 * @param path a [Path] to rewind and set with the new path data. In case provided, this Path would
 *   be the returned one.
 * @param startAngle the angle (in degrees) from which to begin drawing the generated path. By
 *   default, it is set to 0 degrees, meaning the [Path] begins drawing at the 3 o'clock position.
 */
@ExperimentalMaterial3ExpressiveApi
fun Morph.toPath(progress: Float, path: Path = Path(), startAngle: Int = 0): Path {
    return this.toPath(path = path, progress = progress, startAngle = startAngle)
}

/**
 * Returns a [Path] that is computed every time for this [RoundedPolygon].
 *
 * @param startAngle the angle (in degrees) from which to begin drawing the generated path. By
 *   default, it is set to 0 degrees, meaning the [Path] begins drawing at the 3 o'clock position.
 *   The returned path is rotated by this angle around the [RoundedPolygon]'s centroid (centerX,
 *   centerY).
 */
@ExperimentalMaterial3ExpressiveApi
fun RoundedPolygon.toPath(startAngle: Int = 0): Path {
    return this.toPath(path = Path(), startAngle = startAngle, repeatPath = false, closePath = true)

}

/**
 * Returns a [Shape] that is computed every time for this [RoundedPolygon].
 *
 * @param startAngle the angle (in degrees) from which to begin drawing the generated shape's path.
 *   By default, it is set to 0 degrees, meaning the shape's path begins drawing at the 3 o'clock
 *   position. The returned path is rotated by this angle around the [RoundedPolygon]'s centroid
 *   (centerX, centerY).
 */
@ExperimentalMaterial3ExpressiveApi
fun RoundedPolygon.toShape(startAngle: Int = 0): Shape {
    return object : Shape {
        // Store the Path we convert from the RoundedPolygon here. The path we will be
        // manipulating and using on the createOutline would be a copy of this to ensure we
        // don't mutate the original.
        private val shapePath: Path = toPath(startAngle = startAngle)
        private var workPath: Path? = null
        private var lastSize = Size.Unspecified

        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density,
        ): Outline {
            if (size != lastSize || workPath == null) {
                lastSize = size
                // Create a new Path if the size has changed.
                workPath = Path()
            } else {
                workPath!!.rewind()
            }
            val path = workPath!!
            path.addPath(shapePath)
            val scaleMatrix = Matrix().apply { scale(x = size.width, y = size.height) }
            // Scale and translate the path to align its center with the available size
            // center.
            path.transform(scaleMatrix)
            path.translate(size.center - path.getBounds().center)
            return Outline.Generic(path)
        }
    }
}