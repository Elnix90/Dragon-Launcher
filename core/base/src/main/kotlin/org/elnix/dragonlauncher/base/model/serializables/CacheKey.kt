package org.elnix.dragonlauncher.base.model.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.CacheKey.Companion.SEP
import org.elnix.dragonlauncher.base.model.serializables.CacheKey.Companion.compose

/**
 * Stable identity for an installed app, a point, or a resolved launcher icon.
 *
 * A key is a single string assembled from typed parts, and *every* assembly goes through [compose]
 * so the separator and the part ordering live in exactly one place.
 *
 * ## Frozen, persisted formats - do not change
 * The [CacheKey] of an [Application] and the one of a package/user pair are written verbatim into
 * user data: they are the map keys of `AppOverrideState` (aliases, renames, custom icons) and the
 * members of `Workspace.removedAppIds`, and the application key is even shown to the user in the
 * app info sheet. Their layout is `"$packageName#$userId"` and cannot change without a migration,
 * so both constructors deliberately pass **no** kind discriminator.
 *
 * ## In-memory formats
 * Point and icon keys never leave the process. They therefore carry a leading kind discriminator
 * (`point#`, `icon#`), which keeps them apart from each other *and* from the persisted
 * application keys.
 */
@JvmInline
@Serializable
@SerialName("CacheKey")
public value class CacheKey private constructor(
	public val cacheKey: String
) {
	public companion object {
		/**
		 * The single separator used by every key. It is intentionally the same character as the `#`
		 * of the frozen application keys, so one constant rules both formats.
		 */
		private const val SEP = "#"

		/** Kind discriminator for keys that identify a configured point. */
		private const val KIND_POINT = "point"

		/** Kind discriminator for keys that identify a resolved launcher icon. */
		private const val KIND_ICON = "icon"

		/**
		 * Joins [parts] into a single cache key using [SEP].
		 *
		 * Parts are appended verbatim. The mapping stays injective because the trailing integer
		 * parts can never contain [SEP]: a different part list therefore always yields a different
		 * string, so two distinct inputs cannot collide.
		 *
		 * Persisted application keys intentionally pass no kind discriminator, see the class doc.
		 */
		private fun compose(vararg parts: Any?): String = parts.joinToString(SEP)
	}

	/**
	 * Icon constructor for the Icon Service: identifies what is being drawn together with every
	 * input that can change the rendered result, so a resolution is never reused for a different
	 * snapshot of those inputs.
	 *
	 * In-memory only.
	 *
	 * @param subject what is being drawn - an [Application] key, a [Point] key, or an action
	 * @param appsRevision the installed-app snapshot [subject] was resolved against
	 */
	public constructor(
		subject: Any,
		appsRevision: Int,
		customIconHashCode: Int,
		providersHashCode: Int,
		transformationsHashcode: Int
	) : this(
		cacheKey =
			compose(
				KIND_ICON,
				subject,
				appsRevision,
				customIconHashCode,
				providersHashCode,
				transformationsHashcode
			)
	)

	/**
	 * Point constructor, to store the key of a point in the cache.
	 *
	 * In-memory only.
	 */
	public constructor(point: Point) : this(cacheKey = compose(KIND_POINT, point.id, point.action))

	/**
	 * Application constructor, **persisted**.
	 * Its output must not change.
	 * @see Workspace
	 */
	public constructor(app: Application) : this(cacheKey = compose(app.packageName, app.user.hashCode()))

	/**
	 * Package/user constructor, **persisted**.
	 * Its output must not change.
	 * @see Workspace
	 */
	public constructor(packageName: String, userId: Int) : this(cacheKey = compose(packageName, userId))
}
