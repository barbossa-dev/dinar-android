plugins {
    alias(libs.plugins.dinar.android.library)
    alias(libs.plugins.dinar.hilt)
}

android {
    namespace = "app.dinar.core.common"
}

dependencies {
    api(projects.core.model)
    implementation(libs.kotlinx.coroutines.core)
}
