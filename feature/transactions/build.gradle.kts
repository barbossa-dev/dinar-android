plugins {
    alias(libs.plugins.dinar.android.feature)
}

android {
    namespace = "app.dinar.feature.transactions"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.data)
}
