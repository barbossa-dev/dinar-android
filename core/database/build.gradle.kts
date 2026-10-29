plugins {
    alias(libs.plugins.dinar.android.library)
    alias(libs.plugins.dinar.android.room)
    alias(libs.plugins.dinar.hilt)
}

android {
    namespace = "app.dinar.core.database"
}

dependencies {
    api(projects.core.model)
    implementation(libs.kotlinx.coroutines.core)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.kotlin.test)
}
