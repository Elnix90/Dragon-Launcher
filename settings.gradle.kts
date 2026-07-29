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
//    id("io.gitlab.arturgpgbosch.detekt") version "1.23.8"
//    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

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
include(":core:common")
include(":core:settings")
include(":core:enumsui")
include(":core:models")
include(":core:base")
include(":core:shizuku")
include(":core:permissions")
include(":core:profiles")

include(":ui:main")
include(":ui:dragon")
include(":ui:base")
include(":ui:composition")
include(":ui:theme")

include(":core:services:icons")
include(":core:services:system")
include(":core:services:compat")
include(":core:services:badges")
include(":core:services:points")
include(":core:services:recents")
include(":core:services:colors")
include(":core:services:fonts")
include(":core:services:security")
include(":core:services:timer")
include(":core:services:appshortcuts")
include(":core:services:migration")

include(":data:notifications")
include(":data:database")
include(":core:i18n")
include(":data:applications")
include(":data:workspaces")
include(":data:appoverrides")
