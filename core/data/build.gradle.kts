plugins {
    alias(libs.plugins.dinar.android.library)
    alias(libs.plugins.dinar.hilt)
}

android {
    namespace = "app.dinar.core.data"
}

dependencies {
    api(projects.core.common)
    api(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(libs.kotlinx.coroutines.core)
}
