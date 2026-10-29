import java.util.Properties

plugins {
    alias(libs.plugins.dinar.android.application)
    alias(libs.plugins.dinar.android.application.compose)
    alias(libs.plugins.dinar.hilt)
    alias(libs.plugins.kotlin.serialization)
}

/*
 * Release signing is read from `keystore.properties` in the project root (never committed),
 * or from environment variables on CI:
 *   DINAR_KEYSTORE_FILE, DINAR_KEYSTORE_PASSWORD, DINAR_KEY_ALIAS, DINAR_KEY_PASSWORD
 * Run `scripts/create-upload-keystore.sh` once to create the key and the properties file.
 */
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use(::load)
}

fun signingValue(key: String, env: String): String? =
    keystoreProperties.getProperty(key) ?: System.getenv(env)

val releaseKeystorePath = signingValue("storeFile", "DINAR_KEYSTORE_FILE")
val hasReleaseSigning = releaseKeystorePath != null && rootProject.file(releaseKeystorePath).exists()

android {
    namespace = "app.dinar"

    defaultConfig {
        applicationId = "ir.famastudio.dinar"
        versionCode = providers.gradleProperty("dinar.versionCode").getOrElse("1").toInt()
        versionName = providers.gradleProperty("dinar.versionName").getOrElse("1.0.0")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // The UI is Persian; keep only Persian and English resources from libraries.
        resourceConfigurations += listOf("fa", "en")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseKeystorePath!!)
                storePassword = signingValue("storePassword", "DINAR_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "DINAR_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "DINAR_KEY_PASSWORD")
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isCrunchPngs = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                logger.warn(
                    "⚠️  Dinar: no release keystore found (keystore.properties). " +
                        "Signing the release build with the DEBUG key — do not publish it.",
                )
                signingConfigs.getByName("debug")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    // Google Play reads the dependency report from the bundle; keep it out of the APK
    // so stores like Myket and Cafe Bazaar don't receive a block encrypted for Google.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = true
    }

    bundle {
        language {
            // All UI text ships in every install; don't split by language.
            enableSplit = false
        }
    }

    packaging {
        resources.excludes += listOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "/META-INF/*.version",
            "/META-INF/*.kotlin_module",
            "DebugProbesKt.bin",
            "kotlin-tooling-metadata.json",
        )
    }

    lint {
        checkReleaseBuilds = true
        abortOnError = true
        // Persian strings live in code; these checks don't apply to this app.
        disable += listOf("MissingTranslation", "SetTextI18n")
    }
}

dependencies {
    implementation(projects.feature.home)
    implementation(projects.feature.transactions)
    implementation(projects.feature.editor)
    implementation(projects.feature.reports)
    implementation(projects.feature.budgets)
    implementation(projects.feature.settings)

    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.ui)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.test)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.test.ext)
    debugImplementation(libs.androidx.compose.ui.testManifest)
}
