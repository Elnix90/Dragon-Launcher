plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.compose)
    alias(libs.plugins.dragon.serialization)
}

android {
    namespace = "org.elnix.dragonlauncher.common"
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.runtime.annotation)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    implementation(libs.timber)
    implementation(libs.bundles.kotlin)
    implementation(libs.dragon.logging)

    api(libs.androidx.activity)
    api(libs.androidx.compose.ui.unit)
    api(libs.androidx.compose.runtime)

    api(project(":core:base"))
    api(project(":core:i18n"))
    api(project(":core:ktx"))
}

