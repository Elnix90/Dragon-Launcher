plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.database"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)

    api(libs.androidx.roomruntime)
    api(libs.androidx.room)
    ksp(libs.androidx.roomcompiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)


    implementation(project(":core:i18n"))
    implementation(project(":core:ktx"))
    implementation(project(":core:settings"))
    implementation(project(":core:base"))
}
