plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.profiles"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.dragon.logging)
    implementation(libs.core)
    implementation(libs.androidx.annotation)
    implementation(libs.hilt.core)
    implementation(libs.javax.inject)
    implementation(libs.timber)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    api(libs.dagger)
    api(libs.kotlinx.coroutines.core)

    runtimeOnly(libs.kotlinx.coroutines.android)

    api(project(":core:base"))
    implementation(project(":core:ktx"))
    api(project(":core:permissions"))
}