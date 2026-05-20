pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WeatherSnap"

// Application Module
includeBuild("build-logic")
include(":app")

// Core Modular Primitives
include(":core:common")
include(":core:model")
include(":core:domain")
include(":core:database")
include(":core:network")
include(":core:file")
include(":core:designsystem")

// Strictly Isolated Feature Modules
include(":feature:weather")
include(":feature:report")
include(":feature:camera")
include(":feature:history")
include(":feature:settings")
