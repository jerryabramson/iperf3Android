// Top-level build file where you can add configuration options common to all subprojects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
         //For KGP
        classpath(libs.kotlin.gradle.plugin)

        // For KSP
        classpath(libs.symbol.processing.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    //id("com.google.dagger.hilt-android") version "2.48" apply false  // ← Match libs.versions.toml hilt version
}