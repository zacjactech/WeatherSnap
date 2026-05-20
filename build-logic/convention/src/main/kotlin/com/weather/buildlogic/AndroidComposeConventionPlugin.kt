package com.weather.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

/**
 * Convention plugin that enables Jetpack Compose in any Android module.
 *
 * Must be applied AFTER [AndroidApplicationConventionPlugin] or
 * [AndroidLibraryConventionPlugin] so the Android Gradle extension is already registered.
 *
 * Supports both `com.android.application` and `com.android.library` modules by looking
 * up the extension via concrete types rather than the abstract [CommonExtension] interface
 * (Gradle registers under the concrete type, not the supertype).
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Resolve the Android extension regardless of module type.
            // Gradle registers it under the concrete type, so we try both.
            val extension: CommonExtension<*, *, *, *, *> =
                extensions.findByType<ApplicationExtension>()
                    ?: extensions.findByType<LibraryExtension>()
                    ?: error(
                        "weathersnap.android.compose must be applied after " +
                            "weathersnap.android.application or weathersnap.android.library"
                    )
            configureAndroidCompose(extension)
        }
    }

    private fun Project.configureAndroidCompose(
        commonExtension: CommonExtension<*, *, *, *, *>,
    ) {
        commonExtension.apply {
            buildFeatures {
                compose = true
            }
            composeOptions {
                kotlinCompilerExtensionVersion = libs.findVersion("composeCompiler").get().toString()
            }
            dependencies {
                val bom = libs.findLibrary("compose-bom").get()
                add("implementation", platform(bom))
                add("androidTestImplementation", platform(bom))

                add("implementation", libs.findLibrary("compose.ui.tooling.preview").get())
                add("debugImplementation", libs.findLibrary("compose.ui.tooling").get())
                add("implementation", libs.findLibrary("compose.material3").get())
                add("implementation", libs.findLibrary("androidx.activity.compose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.viewmodel.compose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.runtime.compose").get())
            }
        }
    }
}
