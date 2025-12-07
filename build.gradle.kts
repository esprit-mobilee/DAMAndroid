// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // plugins from your version catalog
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // plugin Hilt declared here with version
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
}

// optional but useful: force correct JavaPoet version globally
allprojects {
    configurations.all {
        resolutionStrategy {
            force("com.squareup:javapoet:1.13.0")
        }
    }
}
