plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)  // This should match "kotlin = '1.9.0'" in the TOML
    id("kotlin-parcelize")
    // a voir //alias(libs.plugins.com.google.devtools.ksp.gradle.plugin)
    alias(libs.plugins.com.google.android.libraries.mapsplatform.secrets.gradle.plugin.gradle.plugin)
}

android {
    namespace = "fr.isen.goutalguerin.isensmartcompanion"
    compileSdk = 35

    defaultConfig {
        applicationId = "fr.isen.goutalguerin.isensmartcompanion"
        minSdk = 25
        targetSdk = 35
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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.transport.api)
    implementation(libs.play.services.games)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.activity.compose.v1100)
    implementation(libs.androidx.compose.material3.material3)
    //noinspection UseTomlInstead,UseTomlInstead
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    implementation (libs.retrofit)
    //noinspection UseTomlInstead
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.generativeai)
    implementation(libs.logging.interceptor)
    implementation (libs.retrofit.v290)
    implementation (libs.retrofit2.converter.gson)
    implementation(libs.kotlinx.coroutines.android)
    // add the dependency for the Google AI client SDK for Android
    implementation(libs.generativeai)
    // pour l'historique
    implementation (libs.androidx.room.runtime)
    //kapt (libs.androidx.room.compiler)
    implementation (libs.androidx.room.ktx)

    // Coroutines
    implementation (libs.kotlinx.coroutines.android)
    implementation(libs.androidx.room.common)
    debugImplementation (libs.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}