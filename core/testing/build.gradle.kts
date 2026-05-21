plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.weather.core.testing"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
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
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:file"))

    // Testing dependencies
    implementation(libs.junit)
    implementation(libs.mockk)
    implementation(libs.turbine)
    implementation(libs.kotlinx.coroutines.test)
    
    implementation(libs.androidx.test.ext)
    implementation("androidx.test:runner:1.5.2")
    implementation(libs.hilt.android.testing)
}
