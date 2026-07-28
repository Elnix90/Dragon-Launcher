package org.elnix.dragonlauncher.base

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

public object Constants {
    public object PackageNameLists {
        public val systemLaunchers: List<String> = listOf(
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
        public val knownSocialMediaApps: Set<String> = setOf(
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

        public val knownClockPackages: List<String> = listOf(
            "com.google.android.deskclock",
            "com.android.deskclock",
            "com.samsung.android.clockpackage",
            "com.htc.android.worldclock"
        )
    }

    public object PackageNames {
        public const val SHIZUKU_PACKAGE_NAME: String = "moe.shizuku.privileged.api"
    }

    public object Paths {
        public const val THEMES_DIR: String = "themes"
        public val imageExts: List<String> = listOf("png", "jpg", "jpeg", "webp")
    }

    public object URLs {

        public const val ELNIX90_GITHUB_PROFILE_LINK: String = "https://github.com/Elnix90"
        public const val GITHUB_REPO_LINK: String = "https://github.com/Elnix90/Dragon-Launcher"
        public const val GITHUB_REPO_RELEASES_LINK: String = "$GITHUB_REPO_LINK/releases/latest"
        public const val GITHUB_REPO_ISSUES_LINK: String = "$GITHUB_REPO_LINK/issues/new"
        public const val EXTENSIONS_GITHUB_REPO_LINK: String = "https://github.com/Elnix90/Dragon-Launcher-Extensions"
        public const val DISCORD_INVITE_LINK: String = "https://discord.gg/6UyuP8EBWS"
        public const val REDDIT_LINK: String = "https://www.reddit.com/r/dragonlauncher/"
        public const val MAILTO_LINK: String = "mailto:elnix91@proton.me"
        public const val DRAGON_WEBSITE: String = "https://dragonlauncher.lthb.fr/"
        public const val URL_SHIZUKU_SITE: String = "https://shizuku.rikka.app"
        public const val WEBLATE_LINK: String = "https://hosted.weblate.org/engage/dragon-launcher/"

    }

    public object Settings {
        public const val TOUCH_THRESHOLD_PX: Float = 100f
        public const val COLLIDING_SHAPE_THRESHOLD_PX: Float = 50f
        public const val HOVER_POINT_DURATION: Long = 500L
        public val HOVER_GRADIENT_RADIUS: Dp = 75.dp
        public const val SNAP_STEP_DEG: Float = 15f
    }

    public object Drawer {
        public const val DRAWER_DRAG_DOWN_THRESHOLD: Int = 50
        public const val DRAWER_MAX_DRAG_DOWN: Int = 70
    }

    public object Extensions {
        //        const val INTERNET_PROXY_EXTENSION_PGK = "org.elnix.dragonlauncher.proxy"
//        const val AUTO_UPDATE_EXTENSION_PKG = "org.elnix.dragonlauncher.autoupdate"
        public const val FONT_EXTENSION_PKG: String = "org.elnix.dragonlauncher.fonts"
    }
}