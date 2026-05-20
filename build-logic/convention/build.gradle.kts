plugins {
    `kotlin-dsl`
}

group = "com.weather.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "weathersnap.android.application"
            implementationClass = "com.weather.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "weathersnap.android.library"
            implementationClass = "com.weather.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "weathersnap.android.compose"
            implementationClass = "com.weather.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "weathersnap.android.hilt"
            implementationClass = "com.weather.buildlogic.AndroidHiltConventionPlugin"
        }
    }
}
