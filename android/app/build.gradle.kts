plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Plugin-ul compilatorului Compose (obligatoriu de la Kotlin 2.0)
    alias(libs.plugins.kotlin.compose)
    // KSP: genereaza codul SQL pentru @Dao si @Database din Room la compilare
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.stayfinder.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.stayfinder.app"
        minSdk = 26          // Android 8.0 - cerinta proiectului
        targetSdk = 36
        versionCode = 1
        versionName = "0.2.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)   // collectAsStateWithLifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose) // viewModel() in Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)     // @Preview in Android Studio

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Room (SQLite local) + procesor de adnotari KSP
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Preferences DataStore (flag onboarding + JWT in faza 5)
    implementation(libs.androidx.datastore.preferences)

    // Retrofit + Moshi (Faza 5: conectare la backend Spring Boot)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
}
