plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.compose)
}

android {
    namespace = "org.elnix.dragonlauncher.services.system"
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.dragon.logging)
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)
    implementation(libs.timber)

    api(libs.kotlinx.coroutines.core)

    api(project(":core:ktx"))
    api(project(":core:base"))
    implementation(project(":core:settings"))
}
