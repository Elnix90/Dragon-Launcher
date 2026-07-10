package org.elnix.dragonlauncher.base.model.models

import android.content.pm.ApplicationInfo

public enum class AppCategory {
    Games,
    Audio,
    Video,
    Images,
    Social,
    News,
    Maps,
    Productivity,
    Accessibility,
    Other;

    public companion object {
        public fun mapSystemCategoryToSection(category: Int): AppCategory {
            return when (category) {
                ApplicationInfo.CATEGORY_GAME -> Games

                ApplicationInfo.CATEGORY_AUDIO -> Audio
                ApplicationInfo.CATEGORY_VIDEO -> Video
                ApplicationInfo.CATEGORY_IMAGE -> Images

                ApplicationInfo.CATEGORY_SOCIAL -> Social
                ApplicationInfo.CATEGORY_NEWS -> News
                ApplicationInfo.CATEGORY_MAPS -> Maps

                ApplicationInfo.CATEGORY_PRODUCTIVITY -> Productivity
                ApplicationInfo.CATEGORY_ACCESSIBILITY -> Accessibility

                ApplicationInfo.CATEGORY_UNDEFINED -> Other

                else -> Other
            }
        }

        public fun mapAppToSection(app: ApplicationInfo): AppCategory =
            mapSystemCategoryToSection(app.category)
    }
}