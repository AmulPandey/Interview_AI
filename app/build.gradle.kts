plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    kotlin("kapt")
}

android {
    namespace = "com.example.interviewai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.interviewai"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://192.168.31.28:8080/api/v1/\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"http://192.168.31.28:8080/api/v1/\"")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    /* -------------------- Compose -------------------- */
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    /* -------------------- Navigation -------------------- */
    implementation("androidx.navigation:navigation-compose:2.8.2")

    /* -------------------- Lifecycle -------------------- */
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    /* -------------------- Hilt -------------------- */
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    /* -------------------- Retrofit + OkHttp -------------------- */
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    /* -------------------- Charts -------------------- */
    implementation("com.patrykandpatrick.vico:compose-m3:1.15.0")
    implementation("com.patrykandpatrick.vico:core:1.15.0")

    /* -------------------- Animations -------------------- */
    implementation("com.airbnb.android:lottie-compose:6.5.2")

    /* -------------------- Coroutines -------------------- */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    /* -------------------- DataStore -------------------- */
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("io.coil-kt:coil-compose:2.7.0")

    /* -------------------- Core -------------------- */
    implementation(libs.androidx.core.ktx)

    /* -------------------- Testing -------------------- */
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}