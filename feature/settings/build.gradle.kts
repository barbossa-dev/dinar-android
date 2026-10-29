plugins {
    alias(libs.plugins.dinar.android.feature)
}

android {
    namespace = "app.dinar.feature.settings"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(libs.androidx.core.ktx)
}
