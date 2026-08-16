plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.core.services.icons"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.androidx.palette)
    implementation(libs.settings.core)
    implementation(libs.dragon.logging)
    implementation(libs.core)
    implementation(libs.jakarta.inject)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.roomruntime)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.hilt.core)
    implementation(libs.timber)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    api(libs.dagger)
    api(libs.kotlinx.coroutines.core)

    runtimeOnly(libs.kotlinx.coroutines.android)

    implementation(project(":core:ktx"))
    implementation(project(":core:settings"))

    api(project(":core:base"))
    api(project(":data:database"))
    api(project(":data:applications"))
    api(project(":data:appoverrides"))
    api(project(":core:services:points"))
    api(project(":core:services:colors"))
    api(project(":core:services:appshortcuts"))
}
