plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // for annotation processing
    id("kotlin-kapt")
    // apply Hilt plugin (no version here)
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.esprit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.esprit"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    // ======= Base AndroidX / Compose =======
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.benchmark.common)
    implementation(libs.identity.credential)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ======= ESPRIT Specific =======

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ViewModel + Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Hilt (version comes from root)
    implementation("com.google.dagger:hilt-android:2.57.2") // <-- CHANGE THIS
    kapt("com.google.dagger:hilt-android-compiler:2.57.2") // <-- AND THIS


    // Hilt navigation compose
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Coil (images/logo)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ensure correct JavaPoet version
    implementation("com.squareup:javapoet:1.13.0")

    // Material icons
    implementation("androidx.compose.material:material-icons-core:1.7.5")
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    // OpenStreetMap (osmdroid)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Socket.IO
    implementation("io.socket:socket.io-client:2.1.0")

}

// enforce JavaPoet version in this module too
configurations.all {
    resolutionStrategy {
        force("com.squareup:javapoet:1.13.0")
    }
}
