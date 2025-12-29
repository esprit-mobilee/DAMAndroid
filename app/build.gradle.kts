plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Note: kotlin-compose plugin removed for Kotlin 1.9.x compatibility
    // Compose Compiler is configured via composeOptions below

    // for annotation processing
    id("kotlin-kapt")
    // apply Hilt plugin (no version here)
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // API Key from local.properties
        val apiKey = project.findProperty("OPENAI_API_KEY") as String? ?: ""
        buildConfigField("String", "OPENAI_API_KEY", "\"$apiKey\"")
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
        buildConfig = true  // Enable BuildConfig for API key
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    // Fix kapt dependency on R.jar
    kapt {
        correctErrorTypes = true
        useBuildCache = true
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
    implementation(libs.androidx.compose.animation)
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

    // Hilt (compatible with Kotlin 1.9.x)
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")


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

    // PDFBox-Android
    implementation(libs.pdfbox.android)

    // Socket.IO
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }

    // ZXing for QR codes
    implementation("com.google.zxing:core:3.5.2")

}

// enforce JavaPoet version in this module too
configurations.all {
    resolutionStrategy {
        force("com.squareup:javapoet:1.13.0")
        // Force all Kotlin dependencies to use version 1.9.22 for compatibility with kapt
        force("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.22")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")
        force("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
    }
    
    // Check for Duplicate class error (bcprov-jdk15on vs bcprov-jdk15to18)
    // We force exclusion of the older one
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
    exclude(group = "org.bouncycastle", module = "bcpkix-jdk15on")
}
