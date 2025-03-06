

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)  // This should match "kotlin = '1.9.0'" in the TOML
    id("kotlin-parcelize")
    alias(libs.plugins.com.google.devtools.ksp.gradle.plugin)
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
    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
        }
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
    implementation(libs.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.material3.material3)
    implementation (libs.retrofit)
    //noinspection UseTomlInstead
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.generativeai)
    implementation(libs.logging.interceptor)
    implementation (libs.retrofit2.converter.gson)

    implementation(libs.compiler)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    implementation ("androidx.compose.animation:animation:1.6.0")
    implementation ("androidx.compose.foundation:foundation:1.6.0")
    implementation ("androidx.compose.material:material-icons-extended:1.6.0")

    implementation ("androidx.work:work-runtime-ktx:2.7.1")
    implementation ("com.google.accompanist:accompanist-permissions:0.28.0")

  
    implementation ("androidx.compose.material3:material3:1.2.0")

    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.datastore.preferences)

    // Coroutines
    implementation (libs.kotlinx.coroutines.android)
    debugImplementation (libs.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation (libs.google.api.client)
    implementation (libs.google.oauth.client.jetty)
    implementation (libs.google.api.services.calendar)

    implementation ("com.google.android.gms:play-services-auth:20.7.0")// Pour Google Sign-In
    implementation ("com.google.api-client:google-api-client-android:2.2.0") // Spécifique à Android
    implementation ("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
}

configurations.all {
    resolutionStrategy.force ("com.google.guava:guava:31.0.1-android")
}