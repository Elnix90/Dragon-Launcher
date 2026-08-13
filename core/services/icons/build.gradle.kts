plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.core.services.icons"
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.palette)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)
    implementation(libs.dragon.logging)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":core:ktx"))
    implementation(project(":core:base"))
    implementation(project(":core:settings"))
    implementation(project(":data:database"))
    implementation(project(":data:applications"))
    implementation(project(":data:appoverrides"))
    implementation(project(":core:services:points"))
    implementation(project(":core:services:colors"))
    implementation(project(":core:services:compat"))
    implementation(project(":core:services:appshortcuts"))
}
