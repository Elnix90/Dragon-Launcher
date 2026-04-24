import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

kotlin {
    jvmToolchain(21)
}

extensions.configure<LibraryExtension> {
    namespace = "org.elnix.dragonlauncher.models"
    compileSdk {
        version = release(36)
    }
    defaultConfig {
        minSdk = 26

        consumerProguardFiles("consumer-rules.pro")
    }

    flavorDimensions += "channel"

    productFlavors {
        create("stable") { dimension = "channel" }
        create("beta")   { dimension = "channel" }
        create("fdroid") { dimension = "channel" }
    }

    buildTypes {
        debug {}
        release {}

        create("unminifiedRelease") {
            initWith(getByName("release"))
        }

        create("debuggableRelease") {
            initWith(getByName("release"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.timber)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.core)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    api(libs.kotlinx.coroutines.core)

    api(project(":core:logging"))
    api(project(":core:common"))
    api(project(":core:enumsui"))
    api(project(":core:settings"))
    api(project(":core:shizuku"))
}
