@file:Suppress("ConstPropertyName")

package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.serializables.serializers.OffsetSerializer
import org.jetbrains.annotations.ApiStatus
import kotlin.random.Random


/**
 * # [Point]
 *
 * This is the core interactive unit of Dragon Launcher. Each point represents a position on the
 * screen where a user can swipe or tap to trigger an [action]. Points are organized into [Nest]s,
 * which are radial gesture patterns that activate groups of related actions.
 *
 * ## Core Behavior
 *
 * A point occupies a fixed [offset] from its nest's center. When the user's finger reaches that
 * location during a gesture, the point becomes active and its [action] is queued for execution.
 * The [id] uniquely identifies the point for persistence and diffing across backups and migrations.
 *
 * ## Collision & Shape Snapping
 *
 * By default, a point stays at its [offset]. When [shapeId] is set, the point's visual
 * and touch position snap to the intersection between its [offset] ray and the target shape's
 * boundary. If no intersection exists, the point reverts to [offset]. This enables points to
 * align cleanly to polygon outlines without manual coordinate tweaking.
 * See [Nest] for shape definitions.
 *
 * ## Visual Customization
 *
 * Points support extensive styling:
 * - **Colors**: [backgroundColor], [borderColor], [borderColorSelected], [backgroundColorSelected]
 *   (all in ARGB format).
 * - **Stroke**: [borderStroke] (normal) and [borderStrokeSelected] (active), in dp.
 * - **Size & padding**: [size] (dp), [innerPadding] (dp between border and icon).
 * - **Shape**: [borderShape] and [borderShapeSelected] (e.g., [IconShape.Circle]).
 * - **Opacity**: Global [opacity] multiplier (0.0–1.0) applied to the entire point.
 * - **Icon**: [customIcon] fully overrides the action's default icon. [customActionColor]
 *   overrides the action's color when set.
 * - **Label**: [customName] provides an optional display name for accessibility and debug UIs.
 *
 * ## Advanced Gestures
 *
 * ### Live Nest (Hold-to-Preview)
 * When [liveNestTargetNestId] is set, holding the finger on this point for [liveNestPreviewDelayMs]
 * (default 500 ms) opens an overlay of the target nest, scaled to [liveNestScale] (default 0.65).
 * - [liveNestSnapsToFingerPosition]: If true, the nest centers on the finger; if false, it snaps
 *   to the point's own position.
 * - [liveNestGraceDistancePx]: Extra tolerance radius (px) before dismissal. Use -1 for infinite drag.
 * - [liveNestMainNestOpacityPercent]: Opacity (0–100) of the parent nest while Live Nest is open.
 *   Null disables dimming.
 *
 * ### Cycle Actions (Multi-Stage Hold)
 * [cycleActions] defines a sequence of stages that activate based on hold duration:
 * - **Stage 0** (implicit): The point's main [action], fires immediately on tap/release.
 * - **Stage 1–N**: Each [CycleActionStage] specifies an additional hold delay. Once triggered,
 *   that stage's action becomes active.
 * - [cycleActionStageDefaultDelay]: Default inter-stage delay (ms) when individual stages don't specify.
 * - [cycleActionsLoopDelayMs]: After the final stage, wait this long before looping back to Stage 0.
 *   Null or -1 disables looping.
 *
 * ### Hold & Run (Auto-Fire)
 * [holdAndRunDelayMs] sets a hold duration (ms) after which [holdAndRunAction] fires automatically,
 * without requiring a release. Null disables the feature. If [holdAndRunAction] is null, the main
 * [action] is used instead.
 *
 * ## Haptic Feedback
 *
 * [haptic] provides a custom vibration pattern. It's stored as a map of Boolean (true =
 * vibrate, false = pause) to Int (duration in ms)
 * If not provided, it uses the default haptic of its [Nest]
 *
 * ## Nesting & Ownership
 *
 * [nestId] indicates which [Nest] owns this point. This value is required (cannot be null)
 * and is critical for gesture routing and visual grouping.
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
    val borderStroke: Float? = null,

    /** Border thickness (dp) when the swipe point is selected or active. */
    val borderStrokeSelected: Float? = null,

    /** Border color in ARGB format when not selected. */
    val borderColor: Int? = null,

    /** Background fill color (ARGB) in normal state. */
    val backgroundColor: Int? = null,

    /** Border color in ARGB format when selected. */
    val borderColorSelected: Int? = null,

    /** Background fill color (ARGB) in selected state. */
    val backgroundColorSelected: Int? = null,

    /** Global opacity multiplier (0.0 – 1.0) applied to the whole swipe point. */
    val opacity: Float? = null,

    /**
     * Fully customizable haptic feedback generator.
     * Stores the setting in a map of [Boolean] to [Int].
     * when the boolean is true, it indicates a vibration, and when off a pause.
     * the [Int] value is the duration of the vibration
     */
    val haptic: CustomHapticFeedback? = null,

    /** Optional user-defined display name (labels, accessibility, debug UI). */
    val customName: String? = null,

    /** Inner padding (dp) between border and content. */
    val innerPadding: Int? = null,

    /** Optional override for action color, default (null) will use the action color */
    val customActionColor: Int? = null,

    /** Optional size override */
    val size: Int? = null,

    /**
     * Shape of the border icon, default is a circle
     */
    val borderShape: IconShape? = null,

    /**
     * Shape of the selected border icon, default is a circle
     */
    val borderShapeSelected: IconShape? = null,

    /**
     * Id of the [Nest] to render as a scaled overlay when this point is held.
     * Null means Live Nest is disabled for this point.
     */
    val liveNestTargetNestId: Int? = null,

    /**
     * How long (ms) the user must hold on this point before Live Nest activates.
     * Null falls back to a sensible default (500 ms) defined in the overlay controller.
     */
    val liveNestPreviewDelayMs: Int? = null,

    /**
     * Scale factor applied to the Live Nest radii, range 0.3–1.0.
     * Null defaults to 0.65.
     */
    val liveNestScale: Float? = null,

    /**
     * Extra tolerance radius (px) added beyond the outermost Live Nest ring before an
     * out-of-bounds exit is triggered. Prevents accidental dismissal when the finger
     * drifts slightly outside the circle.
     *  `null` / `0` means no grace (strict bounds).
     *  `-1` means no bounds (infinite drag)
     */
    val liveNestGraceDistancePx: Int? = null,

    /**
     * When non-null and Live Nest is open, the main nest layer is drawn at this opacity (0–100).
     * Null means the option is off (main nest stays fully opaque). Default when enabled is 50.
     */
    val liveNestMainNestOpacityPercent: Int? = null,

    /**
     * Whether if the live nest drawn should have its center exactly where it got activated after the timeout, or if it snaps to its host point position
     */
    val liveNestSnapsToFingerPosition: Boolean? = null,

    /**
     * Ordered list of extra timed stages for Cycle Actions.
     * Null means Cycle Actions is disabled for this point.
     *
     * Stage 0 is always the point's own [action] (base, no threshold).
     * Each entry is Stage[1..N]: [CycleActionStage.triggerTimeMs] is the **additional** hold time
     * after the previous stage (or after finger-down for Stage 1) before that stage becomes current.
     */
    val cycleActions: List<CycleActionStage>? = null,

    /**
     * The default delay that is used to wait between stages
     */
    val cycleActionStageDefaultDelay: Int? = null,

    /**
     * Milliseconds to wait in the "Loop Over" phase before the cycle restarts.
     * When null, the actions doesn't loop; -1 = No loop
     */
    val cycleActionsLoopDelayMs: Int? = null,

    /**
     * Milliseconds of continuous hold after which [action] fires automatically, without release.
     * Null means Hold & Run is disabled for this point.
     *
     * When set, the gesture is consumed as soon as the delay elapses; releasing the finger
     * afterwards does not trigger any additional launch.
     */
    val holdAndRunDelayMs: Int? = null,

    /**
     * When non-null, Hold & Run runs this action instead of the point’s main [action].
     * Null means the same action as tap/release (default).
     */
    val holdAndRunAction: Action? = null

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

//    public fun getPos(compute: () -> Offset): Offset {
//        if (this.collidingShapeId == null) return this.offset
//        return this.pos ?: run {
//            val pos = compute()
//            this.pos = pos
//            pos
//        }
//    }

    val key: CacheKey by lazy { CacheKey(this) }

    public fun getSize(defaultPoint: Point): Dp = (size ?: defaultPoint.size ?: defaultSize).coerceAtLeast(1).dp
    public fun getInnerPadding(defaultPoint: Point): Dp = (innerPadding ?: defaultPoint.innerPadding ?: defaultInnerPadding).coerceAtLeast(1).dp



    override fun compareTo(other: Point): Int = this.id.compareTo(other.id)

    override fun toString(): String = "Point(id = ${this.id}, offset = ${this.offset})"

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
                action = action ?: Action.OpenDragonLauncherSettings(),
                id = id ?: Random.nextInt(),
                nestId = 0
            )


        public const val defaultBorderStroke: Float = 4f
        public const val defaultBorderStrokeSelected: Float = 8f
        public const val defaultOpacity: Float = 1f
        public const val defaultInnerPadding: Int = 5
        public const val defaultSize: Int = 22
        public val defaultBorderShape: IconShape = IconShape.Circle
        public val defaultBorderShapeSelected: IconShape = IconShape.Circle
        public const val defaultLiveNestPreviewDelayMs: Int = 500
        public const val defaultLiveNestScale: Float = 0.65f
        public const val defaultLiveNestGraceDistancePx: Int = 50
        public const val defaultLiveNestSnapsToFingerPosition: Boolean = true
        public const val defaultHoldAndRunDelayMs: Int = 500
        public const val defaultCycleActionsLoopDelayMs: Int = 500
        public const val defaultCycleActionStageDefaultDelay: Int = 500
        public const val defaultLiveNestMainNestOpacityPercent: Int = 50
        public val defaultGlow: CustomGlow = CustomGlow(radius = defaultSize * 1.1f)
        public val defaultGlowSelected: CustomGlow = CustomGlow(radius = defaultSize + 1.3f)

        public val defaultSwipePointsValues: Point = dummySwipePoint(null, -1).copy(
            borderStroke = defaultBorderStroke,
            borderStrokeSelected = defaultBorderStrokeSelected,
            opacity = defaultOpacity,
            innerPadding = defaultInnerPadding,
            size = defaultSize,
            borderShape = defaultBorderShape,
            borderShapeSelected = defaultBorderShapeSelected,
            liveNestPreviewDelayMs = defaultLiveNestPreviewDelayMs,
            liveNestScale = defaultLiveNestScale,
            liveNestGraceDistancePx = defaultLiveNestGraceDistancePx,
            liveNestSnapsToFingerPosition = defaultLiveNestSnapsToFingerPosition,
            holdAndRunDelayMs = defaultHoldAndRunDelayMs,
            cycleActionsLoopDelayMs = defaultCycleActionsLoopDelayMs,
            cycleActionStageDefaultDelay = defaultCycleActionStageDefaultDelay,
            liveNestMainNestOpacityPercent = defaultLiveNestMainNestOpacityPercent,
            glow = defaultGlow,
            glowSelected = defaultGlowSelected
        )

        public object PointsJson: DragonJson<Set<Point>>()
        public object PointJson: DragonJson<Point>()
    }
}

public typealias Points = Map<Int, Point>
