plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.services.compat"
}

dependencies {
    implementation(libs.androidx.datastore.core)
    implementation(libs.dragon.logging)
    implementation(libs.core)
    implementation(libs.androidx.annotation)
    implementation(libs.hilt.core)
    implementation(libs.javax.inject)
    implementation(libs.timber)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    api(libs.dagger)

    implementation(project(":core:base"))
    implementation(project(":core:i18n"))
}
