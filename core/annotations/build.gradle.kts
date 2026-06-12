plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.bundles.kotlin)

    implementation(libs.symbol.processing.api)
    implementation(libs.kotlinpoet)
}