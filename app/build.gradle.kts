plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "1.8.10"
}

android {
    namespace = "com.hal1ucinogen.systembarsmodernizer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hal1ucinogen.systembarsmodernizer"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
configurations.configureEach {
    exclude("androidx.appcompat", "appcompat")
}

dependencies {
    implementation(libs.androidX.core)
//    implementation(libs.androidX.appCompat)
    implementation(libs.androidX.fragment)
    implementation(libs.google.material)
    implementation(libs.bundles.androidX.lifecycle)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    compileOnly("io.github.libxposed:api:100")
    implementation("io.github.libxposed:service:100-1.0.0")
    implementation(libs.brvah)
    implementation(libs.bundles.rikkax)
    implementation(libs.timber)
}