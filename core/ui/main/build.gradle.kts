import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
}

extensions.configure<LibraryExtension> {
    namespace = "org.elnix.dragonlauncher.ui.main"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.reorderable)
    implementation(libs.android.image.cropper)
    implementation(libs.material3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.shapeindicators)
    implementation(libs.shizuku.api)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.collection)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.runtime)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.lazycolumnscrollbar)

    api(libs.androidx.activity.compose)
    api(libs.androidx.activity)
    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.ui.geometry)
    api(libs.androidx.fragment)
    api(libs.androidx.lifecycle.common)
    api(libs.androidx.navigation.common)
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.runtime)

    implementation(project(":core:ui:base"))
    implementation(project(":core:ui:dragon"))
    implementation(project(":core:ui:theme"))
    implementation(project(":core:ui:composition"))

    api(project(":core:base"))
    api(project(":core:models"))
    api(project(":core:common"))
    api(project(":core:enumsui"))
    api(project(":core:settings"))
    implementation(project(":core:logging"))
    implementation(project(":core:services"))
}
