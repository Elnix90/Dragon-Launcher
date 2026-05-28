import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(21)
}

extensions.configure<LibraryExtension> {
    namespace = "org.elnix.dragonlauncher.settings"

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
    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.datastore.core)
    implementation(libs.timber)
    implementation(libs.kotlinx.serialization.json)

    api(libs.androidx.datastore.preferences.core)
    api(libs.kotlinx.coroutines.core)

    implementation(project(":core:base"))
    implementation(project(":core:logging"))
    api(project(":core:common"))
    api(project(":core:enumsui"))
}
