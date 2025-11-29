plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.esprit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.esprit"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // Compose Compiler compatible AGP 8.2.x
        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.10"
        }

    }
}

dependencies {

    // ======================
    // Compose (stable + AGP 8.2.2 compatible)
    // ======================
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ======================
    // AndroidX versions compatibles
    // ======================
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")

    // ======================
    // Navigation Compose
    // ======================
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ViewModel/Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // ======================
    // DataStore
    // ======================
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ======================
    // Retrofit + Gson
    // ======================
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp Logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ======================
    // Coil
    // ======================
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ======================
    // Hilt
    // ======================
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")

    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // ======================
    // Room
    // ======================
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // ======================
    // Fix JavaPoet (problème Room)
    // ======================
    implementation("com.squareup:javapoet:1.13.0")
    implementation("androidx.compose.material:material-icons-core:1.5.1")
    implementation("androidx.compose.material:material-icons-extended:1.5.1")

}
