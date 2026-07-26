@file:Suppress("ConstPropertyName", "NOTHING_TO_INLINE")

package org.elnix.dragonlauncher.base.model.serializables

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.serializables.serializers.ColorSerializer
import org.elnix.dragonlauncher.base.model.serializables.serializers.DpSerializer
import org.elnix.dragonlauncher.base.model.serializables.serializers.OffsetSerializer
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.ktx.takeIfNot
import org.jetbrains.annotations.ApiStatus


/**
 * # [Point]
 *
 * This is the core interactive unit of Dragon Launcher. Each point represents a position on the
 * screen where a user can swipe or tap to trigger an [action]. Points are organized into [Nest]s,
 * which are radial gesture patterns that activate groups of related actions.
 *
 * ## Core Behavior
 *
 * A point occupies a fixed [offset] from its nest's center. When the user's finger drags, the closest point based on its [offset] is activated.
 *
 * The [id] uniquely identifies the point
 *
 * ## Nesting & Ownership
 *
 * [nestId] indicates which [Nest] owns this point. This value is required (cannot be null)
 * and is critical for gesture routing and visual grouping.
 *
 * ## Collision & Shape Snapping
 *
 * By default, a point stays at its [offset]. When [shapeId] is set, the point's visual
 * and touch position snap to the intersection between its [offset] ray and the target shape[IntersectionShape]'s
 * boundary. If no intersection exists, the point reverts to [offset]. This enables points to
 * align cleanly to polygon outlines without manual coordinate tweaking.
 * See [Nest] and [IntersectionShape] for shape definitions.
 *
 * ## Visual Customization
 *
 * Points support extensive styling, each parameter declared specified below, act as an override for the default points.
 * The styling system works like this:
 *
 * `Point.property ?: defaultPoint.property ?: constantProperty`
 *
 * This means, that you can override each points individually or tweak them all by changing the default point
 *
 * ## Advanced Gestures
 *
 * ### Live Nest (Hold-to-Preview)
 * When [liveNestTargetNestId] is set, holding the finger on this point for [liveNestPreviewDelayMs]
 * opens an overlay of the target nest, scaled to [liveNestScale].
 * - [liveNestSnapsToFingerPosition]: If true, the nest centers on the finger; if false, it snaps
 *   to the point's own position.
 * - [liveNestGraceDistance]: Extra tolerance radius before dismissal. Use [Dp.Unspecified] for infinite drag.
 * - [liveNestSubNestOpacityPercent]: Opacity (0–100) of the parent nest while Live Nest is open.
 *
 * ### Cycle Actions (Multi-Stage Hold)
 * [cycleActions] defines a sequence of stages that activate based on hold duration:
 * - **Stage 0** (implicit): The point's main [action], fires immediately on tap/release.
 * - **Stage 1–N**: Each [CycleActionStage] specifies an additional hold delay. Once triggered,
 *   that stage's action becomes active.
 * - [cycleActionsLoopDelayMs]: After the final stage, wait this long before looping back to Stage 0.
 *
 * ### Hold & Run (Auto-Fire)
 * [holdAndRunDelayMs] sets a hold duration after which [holdAndRunAction] fires automatically,
 * without requiring a release. If [holdAndRunAction] is null, the main
 * [action] is used instead.
 *
 * @see Nest
 * @see Action
 * @see CycleActionStage
 * @see CustomIcon
 * @see CustomHapticFeedback
 */
@Immutable
@Serializable
@SerialName("Point")
public data class Point(

    /**
     * The main parameter of any point; it's offset from center position
     *
     * # DO NOT USE DIRECTLY
     * This property is made public to allow usage in other libraries, but it isn't meant to be directly accessed.
     * Use [getPos] rather
     */
    @ApiStatus.Internal
    @Serializable(with = OffsetSerializer::class)
    val offset: Offset,
    /**
     *  Action executed when the swipe point is triggered.
     * This cannot be null as each [Point] should trigger an action
     * When this fails to decode, it should defaults to an arbitrary action instead of crashing the backup
     */
    val action: Action,

    /** Stable unique identifier for persistence, diffing, and migrations. */
    val id: Int,

    /** Which nest this points belongs to, this value cannot be null, as each [Point] belongs to a nest */
    val nestId: Int = 0,

    /**
     * Whether this points snaps to one of the shapes of its [nestId]
     * When null, the point uses its [offset], and when this isn't null, the coordinates are computed based in the
     * intersection between the [offset] and the shape, at the same angle the [offset] points.
     * If no intersection is found, the point uses its [offset]
     * @see [Nest]
     */
    val shapeId: Int? = null,

    /** Fully customizable icon definition overriding default visuals. */
    val customIcon: CustomIcon? = null,

    /**
     * Custom Glow that can be applied to the border
     */
    val glow: CustomGlow? = null,
    /**
     * Custom Glow that can be applied to the border when point is selected
     */
    val glowSelected: CustomGlow? = null,

    /** Border thickness (dp) when the swipe point is not selected. */
    @Serializable(with = DpSerializer::class)
    val borderStroke: Dp? = null,
    /** Border thickness (dp) when the swipe point is selected or active. */
    @Serializable(with = DpSerializer::class)
    val borderStrokeSelected: Dp? = null,

    /** Border color in ARGB format when not selected. */
    @Serializable(with = ColorSerializer::class)
    val borderColor: Color? = null,
    /** Border color in ARGB format when selected. */
    @Serializable(with = ColorSerializer::class)
    val borderColorSelected: Color? = null,

    /** Background fill color (ARGB) in normal state. */
    @Serializable(with = ColorSerializer::class)
    val backgroundColor: Color? = null,
    /** Background fill color (ARGB) in selected state. */
    @Serializable(with = ColorSerializer::class)
    val backgroundColorSelected: Color? = null,

    /**
     * Shape of the border icon, default is a circle
     */
    val borderShape: IconShape? = null,

    /**
     * Shape of the selected border icon, default is a circle
     */
    val borderShapeSelected: IconShape? = null,

    /** Global opacity multiplier (0.0 – 1.0) applied to the whole swipe point. */
    @FloatRange(0.0, 1.0)
    val opacity: Float? = null,

    /**
     * Custom haptic feedback triggered when the point is selected on Main screen
     * @see CustomHapticFeedback
     */
    val haptic: CustomHapticFeedback? = null,

    /** Optional user-defined display name */
    val customName: String? = null,

    /** Inner padding (dp) between border and content. */
    @Serializable(with = DpSerializer::class)
    val innerPadding: Dp? = null,

    /** Optional override for action color, default (null) will use the action color */
    @Serializable(with = ColorSerializer::class)
    val customActionColor: Color? = null,

    /** Optional size override, uses the default point size or **default** point size when not provided */
    @Serializable(with = DpSerializer::class)
    val size: Dp? = null,

    /**
     * Id of the [Nest] to render as a scaled overlay when this point is held.
     * Null means Live Nest is disabled for this point.
     */
    val liveNestTargetNestId: Int? = null,

    /**
     * How long (ms) the user must hold on this point before Live Nest activates.
     */
    val liveNestPreviewDelayMs: Int? = null,

    /**
     * Scale factor applied to the Live Nest radii, range 0.3–1.0.
     */
    val liveNestScale: Float? = null,

    /**
     * Extra tolerance radius (px) added beyond the outermost Live Nest ring before an
     * out-of-bounds exit is triggered. Prevents accidental dismissal when the finger
     * drifts slightly outside the circle.
     *  `null` / `0` means no grace (strict bounds).
     *  `-1` means no bounds (infinite drag)
     */
    @Serializable(with = DpSerializer::class)
    val liveNestGraceDistance: Dp? = null,

    /**
     * When a non-null and Live Nest is open, layers under it are drawn at this opacity (0–100).
     * The opacity is either **added**, or **multiplied** depending on user choice.
     */
    @IntRange(from = 0, to = 100)
    val liveNestSubNestOpacityPercent: Int? = null,

    /**
     * Whether if the live nest drawn should have its center exactly where it got activated after the timeout, or if it snaps to its host point position
     */
    val liveNestSnapsToFingerPosition: Boolean? = null,

    /**
     * Ordered list of extra timed stages for Cycle Actions.
     * Null means Cycle Actions is disabled for this point.
     *
     * Stage 0 is always the point's own [action] (base, no threshold).
     * Each entry is Stage`1..N`: [CycleActionStage.triggerTimeMs] is the **additional** hold time
     * after the previous stage (or after finger-down for Stage 1) before that stage becomes current.
     */
    val cycleActions: List<CycleActionStage>? = null,

    /**
     * Milliseconds to wait in the "Loop Over" phase before the cycle restarts.
     * When null, the actions doesn't loop; -1 = No loop
     */
    @IntRange(from = 0)
    val cycleActionsLoopDelayMs: Int? = null,

    /**
     * Whether if the cycle action loops when reached the end of the stages
     */
    val cycleActionsLoop: Boolean? = null,

    /**
     * Milliseconds of continuous hold after which [action] fires automatically, without release.
     * Null means Hold & Run is disabled for this point.
     *
     * When set, the gesture is consumed as soon as the delay elapses; releasing the finger
     * afterwards does not trigger any additional launch.
     */
    @IntRange(from = 0)
    val holdAndRunDelayMs: Int? = null,

    /**
     * When non-null, Hold & Run runs this action instead of the point’s main [action].
     * Null means the same action as tap/release (default).
     */
    val holdAndRunAction: Action? = null,

    /**
     * When enabled, a sharp angle in the user's drag while hovering this point
     * immediately enters the nested nest instead of waiting for the hold delay.
     */
    val fastActivation: Boolean? = null
) : Comparable<Point> {

    /**
     * Relative position of this point.
     * It is computed by the `PointService` (no access here) located at `core/services/points/src/main/kotlin/org/elnix/dragonlauncher/points/PointsService.kt`
     *
     * ## This is the real position you want to use,
     * as the [offset] parameter is not aware of the potential [shapes][IntersectionShape] in the [Nest]
     */
    @Transient
    var pos: Offset? = null

    public fun getPos(): Offset {
        if (this.shapeId == null) return this.offset
        return this.pos ?: this.offset
    }

//    public fun getPos(compute: () -> Offset, defaultEditing: Boolean = false): Offset {
//        if (this.collidingShapeId == null) return this.offset
//        return this.pos ?: run {
//            val pos = compute()
//            this.pos = pos
//            pos
//        }
//    }

    val key: CacheKey by lazy { CacheKey(this) }


    public inline fun getBorderStroke(selected: Boolean, defaultPoint: Point, defaultEditing: Boolean = false): Dp =
        if (selected) {
            this.borderStrokeSelected ?: defaultPoint.borderStrokeSelected.takeIfNot(defaultEditing) ?: defaultBorderStrokeSelected
        } else {
            this.borderStroke ?: defaultPoint.borderStroke.takeIfNot(defaultEditing) ?: defaultBorderStroke
        }

    public inline fun getBorderColor(selected: Boolean, defaultPoint: Point, extraColors: ExtraColors, defaultEditing: Boolean = false): Color =
        if (selected) {
            this.borderColorSelected ?: defaultPoint.borderColorSelected.takeIfNot(defaultEditing)
        } else {
            this.borderColor ?: defaultPoint.borderColor.takeIfNot(defaultEditing)
        } ?: extraColors.shapes

    public inline fun getBackgroundColor(selected: Boolean, defaultPoint: Point, extraColors: ExtraColors, defaultEditing: Boolean = false): Color =
        if (selected) {
            this.backgroundColorSelected ?: defaultPoint.backgroundColorSelected.takeIfNot(defaultEditing)
        } else {
            this.backgroundColor ?: defaultPoint.backgroundColor.takeIfNot(defaultEditing)
        } ?: extraColors.shapes

    public inline fun getBorderShape(selected: Boolean, defaultPoint: Point, defaultEditing: Boolean = false): IconShape =
        if (selected) {
            this.borderShapeSelected ?: defaultPoint.borderShapeSelected.takeIfNot(defaultEditing) ?: defaultBorderShapeSelected
        } else {
            this.borderShape ?: defaultPoint.borderShape.takeIfNot(defaultEditing) ?: defaultBorderShape
        }

    public inline fun getGlow(selected: Boolean, defaultPoint: Point, defaultEditing: Boolean = false): CustomGlow =
        if (selected) {
            this.glowSelected ?: defaultPoint.glowSelected.takeIfNot(defaultEditing) ?: defaultGlowSelected
        } else {
            this.glow ?: defaultPoint.glow.takeIfNot(defaultEditing) ?: defaultGlow
        }

    public inline fun getOpacity(defaultPoint: Point, defaultEditing: Boolean = false): Float =
        this.opacity ?: defaultPoint.opacity.takeIfNot(defaultEditing) ?: defaultOpacity

    public inline fun getInnerPadding(defaultPoint: Point, defaultEditing: Boolean = false): Dp =
        (this.innerPadding ?: defaultPoint.innerPadding.takeIfNot(defaultEditing) ?: defaultInnerPadding).coerceAtLeast(1.dp)

    public inline fun getSize(defaultPoint: Point, defaultEditing: Boolean = false): Dp =
        (this.size ?: defaultPoint.size.takeIfNot(defaultEditing) ?: defaultSize).coerceAtLeast(1.dp)

    public inline fun getLiveNestPreviewDelayMs(defaultPoint: Point, defaultEditing: Boolean = false): Int =
        this.liveNestPreviewDelayMs ?: defaultPoint.liveNestPreviewDelayMs.takeIfNot(defaultEditing) ?: defaultLiveNestPreviewDelayMs

    public inline fun getLiveNestScale(defaultPoint: Point, defaultEditing: Boolean = false): Float =
        this.liveNestScale ?: defaultPoint.liveNestScale.takeIfNot(defaultEditing) ?: defaultLiveNestScale

    public inline fun getLiveNestGraceDistance(defaultPoint: Point, defaultEditing: Boolean = false): Dp =
        this.liveNestGraceDistance ?: defaultPoint.liveNestGraceDistance.takeIfNot(defaultEditing) ?: defaultLiveNestGraceDistance

    public inline fun getLiveNestSnapsToFingerPosition(defaultPoint: Point, defaultEditing: Boolean = false): Boolean =
        this.liveNestSnapsToFingerPosition ?: defaultPoint.liveNestSnapsToFingerPosition.takeIfNot(defaultEditing)
        ?: defaultLiveNestSnapsToFingerPosition

    public inline fun getHoldAndRunDelayMs(defaultPoint: Point, defaultEditing: Boolean = false): Int =
        this.holdAndRunDelayMs ?: defaultPoint.holdAndRunDelayMs.takeIfNot(defaultEditing) ?: defaultHoldAndRunDelayMs

    public inline fun getCycleActionsStageLoopDelayMs(defaultPoint: Point, defaultEditing: Boolean = false): Int =
        this.cycleActionsLoopDelayMs ?: defaultPoint.cycleActionsLoopDelayMs.takeIfNot(defaultEditing) ?: defaultCycleActionsLoopDelayMs

    public inline fun getCycleActionsStageLoop(defaultPoint: Point, defaultEditing: Boolean = false): Boolean =
        this.cycleActionsLoop ?: defaultPoint.cycleActionsLoop.takeIfNot(defaultEditing) ?: defaultCycleActionsLoop

    public inline fun getLiveNestMainNestOpacityPercent(defaultPoint: Point, defaultEditing: Boolean = false): Int =
        this.liveNestSubNestOpacityPercent ?: defaultPoint.liveNestSubNestOpacityPercent.takeIfNot(defaultEditing)
        ?: defaultLiveNestMainNestOpacityPercent

    public inline fun getHaptic(defaultPoint: Point, defaultEditing: Boolean = false): CustomHapticFeedback? =
        this.haptic ?: defaultPoint.haptic.takeIfNot(defaultEditing) ?: defaultHapticFeedback

    public inline fun getFastActivation(defaultPoint: Point, defaultEditing: Boolean = false): Boolean =
        this.fastActivation ?: defaultPoint.fastActivation.takeIfNot(defaultEditing) ?: defaultFastActivation

    override fun compareTo(other: Point): Int = this.id.compareTo(other.id)

    override fun toString(): String = "Point(id = ${this.id}, offset = ${this.offset}, shapeId = ${this.shapeId})"

//    override fun toString(): String =
//        "Point(\n" +
//                "  offset = ${offset}\n" +
//                "  action = ${action}\n" +
//                "  id = $id\n" +
//                "  nestId = $nestId\n" +
//                "  collidingShapeId = $collidingShapeId\n" +
//                "  customIcon = ${customIcon}\n" +
//                "  glow = ${glow}\n" +
//                "  glowSelected = ${glowSelected}\n" +
//                "  borderStroke = ${borderStroke}\n" +
//                "  borderStrokeSelected = ${borderStrokeSelected}\n" +
//                "  borderColor = ${borderColor}\n" +
//                "  backgroundColor = ${backgroundColor}\n" +
//                "  borderColorSelected = ${borderColorSelected}\n" +
//                "  backgroundColorSelected = ${backgroundColorSelected}\n" +
//                "  opacity = ${opacity}\n" +
//                "  haptic = ${haptic}\n" +
//                "  customName = ${customName}\n" +
//                "  innerPadding = ${innerPadding}\n" +
//                "  customActionColor = ${customActionColor}\n" +
//                "  size = ${size}\n" +
//                "  borderShape = ${borderShape}\n" +
//                "  borderShapeSelected = ${borderShapeSelected}\n" +
//                "  liveNestTargetNestId = ${liveNestTargetNestId}\n" +
//                "  liveNestPreviewDelayMs = ${liveNestPreviewDelayMs}\n" +
//                "  liveNestScale = ${liveNestScale}\n" +
//                "  liveNestGraceDistancePx = ${liveNestGraceDistancePx}\n" +
//                "  liveNestMainNestOpacityPercent = ${liveNestMainNestOpacityPercent}\n" +
//                "  liveNestSnapsToFingerPosition = ${liveNestSnapsToFingerPosition}\n" +
//                "  cycleActions = ${cycleActions}\n" +
//                "  cycleActionStageDefaultDelay = ${cycleActionStageDefaultDelay}\n" +
//                "  cycleActionsLoopDelayMs = ${cycleActionsLoopDelayMs}\n" +
//                "  holdAndRunDelayMs = ${holdAndRunDelayMs}\n" +
//                "  holdAndRunAction = ${holdAndRunAction}\n" +
//                ")"


    public companion object {
        public fun dummySwipePoint(
            action: Action? = null,
            id: Int? = null
        ): Point =
            Point(
                offset = Offset.Zero,
                nestId = 0,
                action = action ?: Action.OpenDragonLauncherSettings(),
                id = id ?: -1
            )


        public val defaultBorderStroke: Dp = 4.dp
        public val defaultBorderStrokeSelected: Dp = 8.dp
        public const val defaultOpacity: Float = 1f
        public val defaultInnerPadding: Dp = 5.dp
        public val defaultSize: Dp = 22.dp
        public val defaultBorderShape: IconShape = IconShape.Circle
        public val defaultBorderShapeSelected: IconShape = IconShape.Circle
        public const val defaultLiveNestPreviewDelayMs: Int = 500
        public const val defaultLiveNestScale: Float = 0.65f
        public val defaultLiveNestGraceDistance: Dp = 50.dp
        public const val defaultLiveNestSnapsToFingerPosition: Boolean = true
        public const val defaultHoldAndRunDelayMs: Int = 500
        public val defaultHapticFeedback: CustomHapticFeedback? = null
        public const val defaultCycleActionsLoopDelayMs: Int = 500
        public const val defaultCycleActionsLoop: Boolean = true
        public const val defaultFastActivation: Boolean = true
        public const val defaultLiveNestMainNestOpacityPercent: Int = 50
        public val defaultGlow: CustomGlow = CustomGlow(radius = defaultSize * 1.05f)
        public val defaultGlowSelected: CustomGlow = CustomGlow(radius = defaultSize * 1.1f)

        public val emptyPoint: Point = dummySwipePoint()

        public inline val Point.isDefault: Boolean
            get() = this.shapeId == null &&
                    this.customIcon == null &&
                    this.glow == null &&
                    this.glowSelected == null &&
                    this.borderStroke == null &&
                    this.borderStrokeSelected == null &&
                    this.borderColor == null &&
                    this.borderColorSelected == null &&
                    this.backgroundColor == null &&
                    this.backgroundColorSelected == null &&
                    this.borderShape == null &&
                    this.borderShapeSelected == null &&
                    this.opacity == null &&
                    this.haptic == null &&
                    this.customName == null &&
                    this.innerPadding == null &&
                    this.customActionColor == null &&
                    this.size == null &&
                    this.liveNestTargetNestId == null &&
                    this.liveNestPreviewDelayMs == null &&
                    this.liveNestScale == null &&
                    this.liveNestGraceDistance == null &&
                    this.liveNestSubNestOpacityPercent == null &&
                    this.liveNestSnapsToFingerPosition == null &&
                    this.cycleActions == null &&
                    this.cycleActionsLoopDelayMs == null &&
                    this.holdAndRunDelayMs == null &&
                    this.holdAndRunAction == null &&
                    this.fastActivation == null

        public inline val Point.isNotDefault: Boolean
            get() = !isDefault

        public object PointsJson : DragonJson<Set<Point>>()
        public object DefaultPointJson : DragonJson<Point>()
    }
}

public typealias Points = Map<Int, Point>
