plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.compose)
}

android {
    namespace = "org.elnix.dragonlauncher.ktx"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.core)

    implementation(libs.androidx.compose.runtime.annotation)
    implementation(libs.androidx.ui)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.dragon.logging)

    implementation(libs.core)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.timber)

    api(libs.androidx.fragment)
    api(libs.androidx.ui.graphics)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.unit)

    runtimeOnly(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.compose.ui.geometry)
    testImplementation(libs.androidx.ui.graphics)
    testRuntimeOnly(libs.androidx.test.core)

    testDebugImplementation(libs.robolectric)
}
