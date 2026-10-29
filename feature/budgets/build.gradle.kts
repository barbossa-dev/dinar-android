plugins {
    alias(libs.plugins.dinar.android.feature)
}

android {
    namespace = "app.dinar.feature.budgets"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.data)
}
