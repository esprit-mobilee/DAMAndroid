// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // plugins from your version catalog
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // Kotlin Compose plugin for Kotlin 2.0+
    alias(libs.plugins.kotlin.compose) apply false

    // plugin Hilt declared here with version (compatible with Kotlin 2.0.21)
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
}

// optional but useful: force correct JavaPoet and Kotlin versions globally
allprojects {
    configurations.all {
        resolutionStrategy {
            force("com.squareup:javapoet:1.13.0")
            // Force all Kotlin dependencies to use version 2.0.21 for compatibility
            force("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.21")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.21")
            force("org.jetbrains.kotlin:kotlin-reflect:2.0.21")
        }
    }
}
