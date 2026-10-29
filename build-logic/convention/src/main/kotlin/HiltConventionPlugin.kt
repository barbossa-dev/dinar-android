import app.dinar.buildlogic.libs
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.google.devtools.ksp")
            dependencies {
                add("ksp", libs.findLibrary("hilt-compiler").get())
            }

            // Pure JVM modules only need the annotations.
            pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                dependencies {
                    add("implementation", libs.findLibrary("hilt-core").get())
                }
            }

            // Android modules get the full Hilt Gradle plugin.
            plugins.withType(AndroidBasePlugin::class.java) {
                apply(plugin = "com.google.dagger.hilt.android")
                dependencies {
                    add("implementation", libs.findLibrary("hilt-android").get())
                }
            }
        }
    }
}
