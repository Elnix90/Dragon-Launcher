plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.compose)
    alias(libs.plugins.dragon.serialization)
}

android {
    namespace = "org.elnix.dragonlauncher.base"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.androidx.ui)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.compose.material3)
    implementation(libs.stringsimilarity)
    implementation(libs.dragon.logging)
    implementation(libs.core)
    implementation(libs.annotations)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime.annotation)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.timber)

    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.serialization.core)
    api(libs.androidx.activity)
    api(libs.androidx.material3)
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.compose.animation.core)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.geometry)
    api(libs.androidx.compose.ui.text)
    api(libs.androidx.compose.ui.unit)
    api(libs.androidx.ui.graphics)

    runtimeOnly(libs.kotlinx.coroutines.android)

    implementation(project(":core:libs:material-shapes"))
    implementation(project(":core:ktx"))
    implementation(project(":core:i18n"))
}

