plugins {
    alias(libs.plugins.dinar.android.library)
    alias(libs.plugins.dinar.android.library.compose)
}

android {
    namespace = "app.dinar.core.ui"
}

dependencies {
    api(projects.core.common)
    api(projects.core.designsystem)
    api(projects.core.model)
}
