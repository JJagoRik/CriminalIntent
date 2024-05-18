// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) version "8.3.2" apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.android.library") version "7.1.2" apply false
    id("org.jetbrains.kotlin.kapt") version "1.6.10" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.5.0" apply false
}