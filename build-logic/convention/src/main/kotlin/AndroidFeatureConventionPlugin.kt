import app.dinar.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

/**
 * A feature module: an Android library with Compose, Hilt, type-safe navigation and
 * the shared :core:ui + :core:designsystem modules wired in.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "dinar.android.library")
            apply(plugin = "dinar.android.library.compose")
            apply(plugin = "dinar.hilt")
            apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:designsystem"))

                add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewModelCompose").get())
                add("implementation", libs.findLibrary("androidx-navigation-compose").get())
                add("implementation", libs.findLibrary("kotlinx-serialization-json").get())

                add("testImplementation", libs.findLibrary("turbine").get())
            }
        }
    }
}
