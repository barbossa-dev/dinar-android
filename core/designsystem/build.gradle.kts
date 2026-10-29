plugins {
    alias(libs.plugins.dinar.android.library)
    alias(libs.plugins.dinar.android.library.compose)
}

android {
    namespace = "app.dinar.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material.iconsExtended)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3.adaptive.navigationSuite)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
}
