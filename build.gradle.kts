plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.hilt) apply false
    // kotlin-android puts the Kotlin Gradle plugin on the classpath; it also provides
    // org.jetbrains.kotlin.jvm, which :core:model applies through dinar.jvm.library.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}
