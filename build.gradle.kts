// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // plugins from your version catalog
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // Note: kotlin-compose plugin is only available for Kotlin 2.0+
    // For Kotlin 1.9.x, we use the Compose Compiler integrated in AGP

    // plugin Hilt declared here with version (compatible with Kotlin 1.9.x)
    id("com.google.dagger.hilt.android") version "2.48" apply false
}

// optional but useful: force correct JavaPoet and Kotlin versions globally
allprojects {
    configurations.all {
        resolutionStrategy {
            force("com.squareup:javapoet:1.13.0")
            // Force all Kotlin dependencies to use version 1.9.22 for compatibility with kapt
            force("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.22")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")
            force("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
        }
    }
}
