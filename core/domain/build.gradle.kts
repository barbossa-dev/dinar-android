plugins {
    alias(libs.plugins.dinar.android.library)
}

android {
    namespace = "app.dinar.core.domain"
}

dependencies {
    api(projects.core.data)
    api(projects.core.model)
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)
}
