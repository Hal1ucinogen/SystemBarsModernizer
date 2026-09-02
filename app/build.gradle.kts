plugins {
    id("com.android.application")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.hal1ucinogen.systembarsmodernizer"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.hal1ucinogen.systembarsmodernizer"
        minSdk = 29
        targetSdk = 37
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
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.serialization.InternalSerializationApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        lintConfig = file("lint.xml")
    }
}
configurations.configureEach {
    exclude("androidx.appcompat", "appcompat")
}

dependencies {
    implementation(libs.androidX.core)
//    implementation(libs.androidX.appCompat)
    implementation(libs.androidX.fragment)
    implementation(libs.androidX.preference)
    implementation(libs.google.material)
    implementation(libs.coil)
    implementation(libs.bundles.androidX.lifecycle)
    implementation(libs.bundles.androidX.room)
    ksp(libs.androidX.room.compiler)
    implementation(libs.kotlinx.serialization.json)
    compileOnly("io.github.libxposed:api:102.0.0")
    implementation("io.github.libxposed:service:102.0.0")
    implementation(libs.brvah)
    implementation(libs.bundles.rikkax)
    implementation(libs.timber)
    implementation(libs.fastScroll)
    implementation(libs.bundles.zhaobozhen)
}