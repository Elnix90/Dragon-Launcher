import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
}

android {
    namespace = "org.elnix.dragonlauncher.ui.main"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.compose.animation.graphics)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.process)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.material3)
    implementation(libs.reorderable)
    implementation(libs.shapeindicators)
    implementation(libs.shizuku.api)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.collection)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.lazycolumnscrollbar)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.datetime)
    implementation(libs.timber)
    implementation(libs.bundles.kotlin)
    implementation(libs.coil.core)
    implementation(libs.coil.compose)
    implementation(libs.dragon.logging)
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)

    api(libs.androidx.activity.compose)
    api(libs.androidx.activity)
    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.ui.geometry)
    api(libs.androidx.fragment)
    api(libs.androidx.lifecycle.common)
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.runtime)

    implementation(project(":core:ui:dragon"))
    implementation(project(":core:ui:theme"))
    implementation(project(":core:ui:composition"))
    api(project(":core:ui:base"))

    api(project(":core:base"))
    api(project(":core:ktx"))
    api(project(":core:models"))
    api(project(":core:i18n"))
    api(project(":core:common"))
    api(project(":core:enumsui"))
    api(project(":core:settings"))

    implementation(project(":core:ktx"))
    implementation(project(":core:shizuku"))
    implementation(project(":core:profiles"))

    implementation(project(":core:permissions"))
    implementation(project(":data:notifications"))
    implementation(project(":data:applications"))

    implementation(project(":core:services:system"))
}
