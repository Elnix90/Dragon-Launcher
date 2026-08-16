import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.compose)
    alias(libs.plugins.dragon.serialization)
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
}

android {
    namespace = "org.elnix.dragonlauncher.ui.main"
}

dependencies {
    implementation(libs.androidx.lifecycle.process)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
    implementation(libs.timber)
    implementation(libs.coil.compose)
    implementation(libs.coil.compose.base)
    implementation(libs.dragon.logging)
    implementation(libs.settings.runtime)
    implementation(libs.compose.lock)
    implementation(libs.shapeindicators)
    implementation(libs.shizuku.api)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.collection)
    implementation(libs.lazycolumnscrollbar)
    implementation(libs.core)
    implementation(libs.androidx.lifecycle.runtime.base)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.compose.runtime.retain)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)

    runtimeOnly(libs.kotlinx.coroutines.android)
    runtimeOnly(project(":data:notifications"))

    api(libs.androidx.activity.compose)
    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.ui.geometry)
    api(libs.androidx.lifecycle.common)
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.ui.text)
    api(libs.androidx.compose.ui.unit)
    api(libs.androidx.material3)
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.ui)
    api(libs.androidx.ui.graphics)
    api(libs.reorderable)
    api(libs.settings.core)

    implementation(project(":ui:dragon"))
    implementation(project(":ui:theme"))
    implementation(project(":ui:composition"))
    implementation(project(":ui:base"))

    api(project(":core:base"))
    implementation(project(":core:i18n"))
    api(project(":core:models"))
    api(project(":core:enumsui"))
    api(project(":core:settings"))

    implementation(project(":core:ktx"))
    implementation(project(":core:shizuku"))
    implementation(project(":core:permissions"))
    implementation(project(":core:services:system"))
    implementation(project(":core:services:fonts"))
    implementation(project(":core:services:migration"))
    implementation(project(":core:services:timer"))
    implementation(project(":data:appoverrides"))
    implementation(project(":data:workspaces"))

    api(project(":core:services:badges"))
    api(project(":core:services:icons"))
    api(project(":core:services:points"))

    debugApi(libs.reorderable.android.debug)
}
