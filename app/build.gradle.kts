plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    // ✅ Firebase — required for google-services.json to work
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.know_it_all"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.know_it_all"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://192.168.0.107:8080/api/v1/\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"https://your-railway-url.up.railway.app/api/v1/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation
    implementation(libs.navigation.compose)

    // ViewModel & Lifecycle
    implementation(libs.viewmodel.compose)
    implementation(libs.runtime.compose)

    // ✅ Firebase BOM — controls all Firebase library versions
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // ✅ Firebase Auth — login, register, logout
    implementation("com.google.firebase:firebase-auth-ktx")

    // ✅ Firebase Firestore — database (replaces Room + Retrofit)
    implementation("com.google.firebase:firebase-firestore-ktx")

    // ✅ Firebase Coroutines support
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Retrofit & Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Security & Encryption
    implementation(libs.security.crypto)

    // Location Services
    implementation(libs.play.services.location)

    // Image Loading
    implementation(libs.coil.compose)

    // Accompanist
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.systemuicontroller)

    // OSMDroid map
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // iText PDF
    implementation(libs.itext.kernel)
    implementation(libs.itext.io)
    implementation(libs.itext.layout)
    implementation(libs.itext.forms)

    // ZXing QR
    implementation(libs.zxing.core)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Pdf-Sharing
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
}