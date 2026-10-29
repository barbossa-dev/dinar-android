import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "app.dinar.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    compileOnly(libs.findLibrary("android.gradlePlugin").get())
    compileOnly(libs.findLibrary("compose.gradlePlugin").get())
    compileOnly(libs.findLibrary("kotlin.gradlePlugin").get())
    compileOnly(libs.findLibrary("ksp.gradlePlugin").get())
    compileOnly(libs.findLibrary("room.gradlePlugin").get())
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "dinar.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "dinar.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "dinar.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "dinar.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "dinar.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidRoom") {
            id = "dinar.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("hilt") {
            id = "dinar.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("jvmLibrary") {
            id = "dinar.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}