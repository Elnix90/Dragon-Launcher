import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
}


kotlin {
    jvmToolchain(21)
}

extensions.configure<LibraryExtension> {
    namespace = "org.elnix.dragonlauncher.database"

    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {

    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    api(libs.androidx.roomruntime)
    ksp(libs.androidx.roomcompiler)
    api(libs.androidx.room)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":core:i18n"))
    implementation(project(":core:ktx"))
    implementation(project(":core:settings"))
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)
    implementation(project(":core:base"))

}