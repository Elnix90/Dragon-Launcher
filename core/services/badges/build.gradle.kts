plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.services.badges"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.jakarta.inject)
    implementation(libs.androidx.annotation)
    implementation(libs.hilt.core)
    implementation(libs.hilt.android)

    ksp(libs.hilt.compiler)

    api(libs.dagger)
    api(libs.kotlinx.coroutines.core)

    runtimeOnly(libs.kotlinx.coroutines.android)
    runtimeOnly(libs.androidx.datastore.core)

    implementation(project(":core:i18n"))
    api(project(":core:base"))
    api(project(":core:profiles"))
    api(project(":data:notifications"))
}
