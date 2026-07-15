// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "9.2.1" apply false
    alias(libs.plugins.kotlin.android) apply false
    kotlin("plugin.serialization") version "1.8.10"
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.ksp) apply false
}