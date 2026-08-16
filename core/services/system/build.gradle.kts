plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.compose)
}

android {
    namespace = "org.elnix.dragonlauncher.services.system"
}

dependencies {
    implementation(libs.core)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.dragon.logging)
    implementation(libs.settings.core)
    implementation(libs.timber)

    api(libs.kotlinx.coroutines.core)

    implementation(project(":core:ktx"))
    implementation(project(":core:settings"))
    api(project(":core:base"))
}
