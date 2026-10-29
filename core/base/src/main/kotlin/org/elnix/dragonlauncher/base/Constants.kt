package org.elnix.dragonlauncher.base

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Constants {
	object PackageNameLists {
		val systemLaunchers: List<String> =
			listOf(
				// Xiaomi/RedMagic/HyperOS/MIUI
				"com.miui.home",
				"com.miui.home.launcher",
				"com.zui.launcher",
				"com.redmagic.launcher",
				// Samsung OneUI
				"com.sec.android.app.launcher",
				"com.samsung.android.app.launcher",
				// ZTE/Nubia
				"com.zte.mifavor.launcher",
				"com.android.nubialauncher",
				// OnePlus OxygenOS/ColorOS
				"com.oneplus.launcher",
				"com.oplus.launcher",
				// OPPO/Realme
				"com.oppo.launcher",
				"com.coloros.safecenter.launcher",
				// Huawei EMUI/HarmonyOS
				"com.huawei.android.launcher",
				"com.huawei.android.home",
				// Google Pixel/Stock Android
				"com.google.android.apps.nexuslauncher",
				"com.android.launcher3",
				// Sony
				"com.sonymobile.home",
				// LG
				"com.lge.launcher2",
				"com.lge.launcher3",
				// HTC
				"com.htc.launcher",
				// Motorola
				"com.motorola.blur.launcher",
				// Vivo FuntouchOS
				"com.iuni.launcher",
				// Nothing OS
				"com.nothing.launcher",
				// Fairphone
				"ch.fairphone.launcher"
			)

		/**
		 * Known social media package names for auto-detection
		 */
		val knownSocialMediaApps: Set<String> =
			setOf(
				// Meta
				"com.instagram.android",
				"com.facebook.katana",
				"com.facebook.orca", // Messenger
				"com.whatsapp",
				"com.facebook.lite",
				// ByteDance
				"com.zhiliaoapp.musically", // TikTok
				"com.ss.android.ugc.trill", // TikTok (alternate)
				// Snap
				"com.snapchat.android",
				// Twitter/X
				"com.twitter.android",
				"com.twitter.android.lite",
				// Reddit
				"com.reddit.frontpage",
				// Pinterest
				"com.pinterest",
				// LinkedIn
				"com.linkedin.android",
				// Telegram
				"org.telegram.messenger",
				"org.telegram.messenger.web",
				// Discord
				"com.discord",
				"com.aliucord",
				// BeReal
				"com.bereal.ft",
				// Threads
				"com.instagram.barcelona",
				// YouTube (can be considered social)
				"com.google.android.youtube",
				// Twitch
				"tv.twitch.android.app",
				// Tumblr
				"com.tumblr",
				// WeChat
				"com.tencent.mm"
			)

		val knownClockPackages: List<String> =
			listOf(
				"com.google.android.deskclock",
				"com.android.deskclock",
				"com.samsung.android.clockpackage",
				"com.htc.android.worldclock"
			)
	}

	object Signatures {
		const val DRAGON_LAUNCHER_SIGNATURE_HASH: String = "63068d94e01eeae50efcb2a0c43dfa8ac503a421cdeaf353d45b69ab933c0a06"
	}

	object PackageNames {
		const val SHIZUKU_PACKAGE_NAME: String = "moe.shizuku.privileged.api"
	}

	object Paths {
		const val THEMES_DIR: String = "themes"
		val imageExts: List<String> = listOf("png", "jpg", "jpeg", "webp")
	}

	object URLs {
		const val ELNIX90_GITHUB_PROFILE_LINK: String = "https://github.com/Elnix90"
		const val ELNIX90_BUY_ME_A_COFFEE: String = "https://buymeacoffee.com/elnix90"
		const val GITHUB_REPO_LINK: String = "https://github.com/Elnix90/Dragon-Launcher"
		const val GITLAB_REPO_LINK: String = "https://gitlab.com/Elnix91/Dragon-Launcher"
		const val CODEBERG_REPO_LINK: String = "https://codeberg.org/Elnix90/Dragon-Launcher"
		const val GITHUB_REPO_RELEASES_LINK: String = "$GITHUB_REPO_LINK/releases/latest"
		const val GITHUB_REPO_ISSUES_LINK: String = "$GITHUB_REPO_LINK/issues/new"
		const val EXTENSIONS_GITHUB_REPO_LINK: String = "https://github.com/Elnix90/Dragon-Launcher-Extensions"
		const val DISCORD_INVITE_LINK: String = "https://discord.gg/6UyuP8EBWS"
		const val REDDIT_LINK: String = "https://www.reddit.com/r/dragonlauncher/"
		const val MAILTO_LINK: String = "mailto:elnix91@proton.me"
		const val TELEGRAM_LINK: String = "https://t.me/elnix90"
		const val DRAGON_WEBSITE: String = "https://dragonlauncher.lthb.fr/"
		const val DOCUMENTATION_LINK: String = "https://elnix90.github.io/Dragon-Launcher/"
		const val URL_SHIZUKU_SITE: String = "https://shizuku.rikka.app"
		const val WEBLATE_LINK: String = "https://hosted.weblate.org/engage/dragon-launcher/"
	}

	object Settings {
		const val TOUCH_THRESHOLD_PX: Float = 100f
		const val COLLIDING_SHAPE_THRESHOLD_PX: Float = 50f
		const val HOVER_POINT_DURATION: Long = 500L
		val HOVER_GRADIENT_RADIUS: Dp = 75.dp

		/**
		 * Max items allowed in the settings drop down menu, for UX reasons, otherwise it would be a scrolling thing, and I don't like it that much.
		 *
		 *
		 * TODO This SHOULD be removed one day, maybe next time I read this, and implement a scrolling menu with maximum size.
		 *
		 * For now, I'll only increase it a bit. from 5 to
		 */
		const val MAX_ITEMS_ALLOWED: Int = 8

		const val DOUBLE_CLICK_ACTION_DELAY: Int = 500
	}

	object Drawer {
		const val DRAWER_DRAG_DOWN_THRESHOLD: Int = 50
		const val DRAWER_MAX_DRAG_DOWN: Int = 70
	}

	object Extensions {
		//        const val INTERNET_PROXY_EXTENSION_PGK = "org.elnix.dragonlauncher.proxy"
//        const val AUTO_UPDATE_EXTENSION_PKG = "org.elnix.dragonlauncher.autoupdate"
		const val FONT_EXTENSION_PKG: String = "org.elnix.dragonlauncher.fonts"
	}
}
