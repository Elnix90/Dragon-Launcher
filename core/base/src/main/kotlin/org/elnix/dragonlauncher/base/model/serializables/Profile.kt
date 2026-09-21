package org.elnix.dragonlauncher.base.model.serializables

import android.content.Context
import android.os.Process
import android.os.UserHandle
import androidx.annotation.StringRes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.serializables.serializers.UserHandleSerializer
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.getSerialNumber

@Serializable
@SerialName("Profile")
public data class Profile(
	val type: Type,
	@Serializable(with = UserHandleSerializer::class)
	val userHandle: UserHandle,
	val serial: Long
) {
	override fun equals(other: Any?): Boolean {
		if (other is Profile) {
			return userHandle == other.userHandle
		}
		return super.equals(other)
	}

	override fun hashCode(): Int = userHandle.hashCode()

	public enum class Type(
		@StringRes
		public val resId: Int
	) {
		/**
		 * The default profile.
		 */
		Personal(R.string.personal_profile),

		/**
		 * The work profile.
		 */
		Work(R.string.work_profile),

		/**
		 * The private space profile (Android 15+)
		 */
		Private(R.string.private_profile)
	}

	public data class State(
		val locked: Boolean = false
	)

	override fun toString(): String = "Profile(type = $type, userHandle = $userHandle, serial = $serial)"

	public companion object {
		public fun fromUserHandle(ctx: Context, userHandle: UserHandle): Profile {
			val serial = userHandle.getSerialNumber(ctx)
			return Profile(Type.Personal, userHandle, serial)
		}

		public fun dummy(): Profile = Profile(Type.Personal, Process.myUserHandle(), 0L)
	}
}
