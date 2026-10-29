plugins {
    alias(libs.plugins.dinar.android.library)
    alias(libs.plugins.dinar.hilt)
}

android {
    namespace = "app.dinar.core.datastore"
}

dependencies {
    api(projects.core.model)
    api(libs.androidx.dataStore.preferences)
    implementation(projects.core.common)
    implementation(libs.kotlinx.coroutines.core)
}
