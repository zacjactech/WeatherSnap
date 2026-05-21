plugins {
    id("weathersnap.android.application")
    id("weathersnap.android.compose")
    id("weathersnap.android.hilt")
}

android {
    namespace = "com.weather.snap"

    defaultConfig {
        applicationId = "com.weather.snap"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "com.weather.core.testing.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystoreFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            val releaseSigning = signingConfigs.findByName("release")
            if (releaseSigning != null && releaseSigning.storeFile?.exists() == true) {
                signingConfig = releaseSigning
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Feature Module Bindings
    implementation(project(":feature:weather"))
    implementation(project(":feature:report"))
    implementation(project(":feature:camera"))
    implementation(project(":feature:history"))

    // Core Infrastructures Module Bindings
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:file"))

    // Compose Navigation & Hilt Nav
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Splash Screen


    // Testing Support
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    androidTestImplementation(project(":core:testing"))
}
