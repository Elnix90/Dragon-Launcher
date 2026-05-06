package org.elnix.dragonlauncher.common.messyfolder

import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.logging.LogTag


object Constants {
    object PackageNameLists {
        val systemLaunchers = listOf(
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
        val knownSocialMediaApps = setOf(
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

        val knownClockPackages = listOf(
            "com.google.android.deskclock",
            "com.android.deskclock",
            "com.samsung.android.clockpackage",
            "com.htc.android.worldclock"
        )
    }

    object PackageNames {
        const val SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api"
    }

    object Paths {

        /* Themes loader utils */
        const val THEMES_DIR = "themes"
        val imageExts = listOf("png", "jpg", "jpeg", "webp")
    }

    object Actions {
        val defaultChoosableActions: Set<SwipeActionSerializable> = setOf(
            SwipeActionSerializable.OpenCircleNest(0),
            SwipeActionSerializable.GoParentNest,
            SwipeActionSerializable.LaunchApp("", false, 0),
            SwipeActionSerializable.LaunchShortcut("", ""),
            SwipeActionSerializable.OpenUrl(""),
            SwipeActionSerializable.OpenFile(""),
            SwipeActionSerializable.NotificationShade,
            SwipeActionSerializable.ControlPanel,
            SwipeActionSerializable.OpenAppDrawer(),
            SwipeActionSerializable.Lock,
            SwipeActionSerializable.ReloadApps,
            SwipeActionSerializable.OpenRecentApps,
            SwipeActionSerializable.OpenDragonLauncherSettings(),
            SwipeActionSerializable.RunAdbCommand(""),
            SwipeActionSerializable.ToggleBluetooth(),
            SwipeActionSerializable.ToggleWifi(),
            SwipeActionSerializable.ToggleData(),
        )
    }

    object Logging {
        /*  ─────────────  Tags constants  ─────────────  */
        val TAG = LogTag("DragonLauncherDebug")
        val APPS_TAG = LogTag("AppsVm")
        val ICONS_TAG = LogTag("IconsDebug")
        val BACKUP_TAG = LogTag("SettingsBackupManager")
        val SWIPE_TAG = LogTag("SwipeDebug")
        val WIDGET_TAG = LogTag("WidgetsDebug")
        val FLOATING_APPS_TAG = LogTag("FloatingAppsDebug")
        val ACCESSIBILITY_TAG = LogTag("SystemControl")
        val IMAGE_TAG = LogTag("ImageDebug")

        //        val SETTINGS_TAG = LogTag("SettingsDebug")
//        val ICON_PACK_TAG = LogTag("IconsPacks")
        val SHAPES_TAG = LogTag("ShapesDebug")

        //        val COLORS_TAG = LogTag("ColorsDebug")
        val DRAWER_TAG = LogTag("DrawerDebug")
        val APP_LAUNCH_TAG = LogTag("AppLaunchDebug")
        val PRIVATE_SPACE_TAG = LogTag("PrivateSpaceDebug")

        //        val APPS_JSON_TAG = LogTag("AppsJsonDebug")
        val PM_COMPAT_TAG = LogTag("PmCompatDebug")
        val BROADCAST_TAG = LogTag("BroadcastDebug")

        //        val POINTS_TAG = LogTag("PointsDebug")
        val STATUS_BAR_TAG = LogTag("StatusBarDebug")
        val WORKSPACES_TAG = LogTag("WorkspacesDebug")
        val NESTS_TAG = LogTag("NestsDebug")
        val ANGLE_LINE_TAG = LogTag("AngleLineDebug")
        val HOLD_TAG = LogTag("HoldDebug")
        val LAUNCHER_WIDGET_HOLDER_TAG = LogTag("LauncherWidgetHolder")
        val PRIVATE_SPACE_UTILS = LogTag("PrivateSpaceUtils")
        val PINNED_SHORTCUTS = LogTag("PinnedShortcuts")
        val SECURITY_HELPER = LogTag("SecurityHelper")
        val LOGS_TAG = LogTag("LogsTab")
        val STARTUP_TAG = LogTag("StartupPerf")
        val OVERLAY_REMINDER_TAG = LogTag("OverlayReminder")
        val FONT_RECEIVER_TAG = LogTag("FontsReceiver")
        val FONT_PROVIDER = LogTag("FontsProvider")
        val SAMSUNG_INTEGRATION_TAG = LogTag("SamsungIntegration")
        val EXTENSION_MANAGER_TAG = LogTag("ExtensionManager")
        val HAPTIC_TAG = LogTag("HapticDebug")
        val WELCOME_TAG = LogTag("WelcomeDebug")
        val MAIN_SCREEN_LAYERS_TAG = LogTag("MainScreenLayersTag")
        val SHIZUKU_TAG = LogTag("ShizukuDebug")
        val NAVIGATION_TAG = LogTag("NavigationDebug")
        val THEMES_TAG = LogTag("ThemeDebug")
        val CONTEXT_TAG = LogTag("ContextDebug")
//        val SPECIAL_TAG = LogTag("SpecialDebug")
    }

    /*  ─────────────  Links  ─────────────  */
    object URLs {

        const val ELNIX90_GITHUB_PROFILE_LINK = "https://github.com/Elnix90"
        const val GITHUB_REPO_LINK = "https://github.com/Elnix90/Dragon-Launcher"
        const val GITHUB_REPO_RELEASES_LINK = "$GITHUB_REPO_LINK/releases/latest"
        const val GITHUB_REPO_ISSUES_LINK = "$GITHUB_REPO_LINK/issues/new"
        const val EXTENSIONS_GITHUB_REPO_LINK = "https://github.com/Elnix90/Dragon-Launcher-Extensions"
        const val DISCORD_INVITE_LINK = "https://discord.gg/6UyuP8EBWS"
        const val REDDIT_LINK = "https://www.reddit.com/r/dragonlauncher/"
        const val MAILTO_LINK = "mailto:elnix91@proton.me"
        const val DRAGON_WEBSITE = "https://dragonlauncher.lthb.fr/"
        const val URL_SHIZUKU_SITE = "https://shizuku.rikka.app"
        const val WEBLATE_LINK = "https://hosted.weblate.org/engage/dragon-launcher/"

    }

    object Settings {
        /*  ─────────────  Settings Screen Constants  ─────────────  */
        const val POINT_HITBOX_RADIUS_PX = 40f
        const val TOUCH_THRESHOLD_PX = 100f
        const val HOVER_POINT_DURATION = 500L
        const val HOVER_GRADIENT_RADIUS = 75f
        const val SNAP_STEP_DEG = 15.0
        const val HOME_REENTER_WINDOW_MS = 80L

        const val STATUS_BAR_TEMPLATE = "[\n" +
                "    {\n" +
                "      \"type\": \"org.elnix.dragonlauncher.common.serializables.StatusBarSerializable.Time\",\n" +
                "      \"formatter\": \"HH:mm:ss | \",\n" +
                "      \"action\": null,\n" +
                "      \"fontSize\": 16,\n" +
                "      \"isBold\": false,\n" +
                "      \"colorHex\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"org.elnix.dragonlauncher.common.serializables.StatusBarSerializable.Date\",\n" +
                "      \"formatter\": \"MMM dd\",\n" +
                "      \"action\": null,\n" +
                "      \"fontSize\": 14,\n" +
                "      \"isBold\": false,\n" +
                "      \"colorHex\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"org.elnix.dragonlauncher.common.serializables.StatusBarSerializable.Spacer\",\n" +
                "      \"width\": -1\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"org.elnix.dragonlauncher.common.serializables.StatusBarSerializable.Notifications\",\n" +
                "      \"maxIcons\": 8,\n" +
                "      \"iconSize\": 18\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"org.elnix.dragonlauncher.common.serializables.StatusBarSerializable.Spacer\",\n" +
                "      \"width\": 6\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"org.elnix.dragonlauncher.common.serializables.StatusBarSerializable.Bandwidth\",\n" +
                "      \"merge\": false,\n" +
                "      \"fontSize\": 12,\n" +
                "      \"colorHex\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"org.elnix.dragonlauncher.common.serializables.StatusBarSerializable.Spacer\",\n" +
                "      \"width\": 7\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"org.elnix.dragonlauncher.common.serializables.StatusBarSerializable.Connectivity\",\n" +
                "      \"showAirplaneMode\": true,\n" +
                "      \"showWifi\": true,\n" +
                "      \"showBluetooth\": true,\n" +
                "      \"showVpn\": true,\n" +
                "      \"showMobileData\": true,\n" +
                "      \"showHotspot\": true,\n" +
                "      \"showUsb\": true,\n" +
                "      \"updateFrequency\": 5,\n" +
                "      \"iconSize\": 18\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"org.elnix.dragonlauncher.common.serializables.StatusBarSerializable.Spacer\",\n" +
                "      \"width\": 10\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"org.elnix.dragonlauncher.common.serializables.StatusBarSerializable.Battery\",\n" +
                "      \"showIcon\": false,\n" +
                "      \"showPercentage\": true,\n" +
                "      \"fontSize\": 14,\n" +
                "      \"colorHex\": null\n" +
                "    }\n" +
                "  ]"
    }

    object Drawer {
        const val DRAWER_DRAG_DOWN_THRESHOLD = 50
        const val DRAWER_MAX_DRAG_DOWN = 70
    }

    object Extensions {
        //        const val INTERNET_PROXY_EXTENSION_PGK = "org.elnix.dragonlauncher.proxy"
//        const val AUTO_UPDATE_EXTENSION_PKG = "org.elnix.dragonlauncher.autoupdate"
        const val FONT_EXTENSION_PKG = "org.elnix.dragonlauncher.fonts"
    }
}
