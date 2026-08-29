pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

includeBuild("build-logic")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Dragon Launcher"
include(":app")

include(":core:libs:material-shapes")

include(":core:ktx")
include(":core:base")
include(":core:models")
include(":core:shizuku")
include(":core:settings")
include(":core:profiles")
include(":core:permissions")

include(":ui:main")
include(":ui:base")
include(":ui:theme")
include(":ui:dragon")
include(":ui:composition")

include(":core:services:icons")
include(":core:services:timer")
include(":core:services:fonts")
include(":core:services:system")
include(":core:services:compat")
include(":core:services:badges")
include(":core:services:points")
include(":core:services:colors")
include(":core:services:widgets")
include(":core:services:recents")
include(":core:services:security")
include(":core:services:migration")
include(":core:services:appshortcuts")

include(":core:i18n")
include(":data:database")
include(":data:workspaces")
include(":data:appoverrides")
include(":data:applications")
include(":data:notifications")
