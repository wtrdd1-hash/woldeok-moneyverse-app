plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

val uploadStoreFile = providers.environmentVariable("ANDROID_UPLOAD_STORE_FILE").orNull
val uploadStorePassword = providers.environmentVariable("ANDROID_UPLOAD_STORE_PASSWORD").orNull
val uploadKeyAlias = providers.environmentVariable("ANDROID_UPLOAD_KEY_ALIAS").orNull
val uploadKeyPassword = providers.environmentVariable("ANDROID_UPLOAD_KEY_PASSWORD").orNull
val releaseRequested = gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }

if (releaseRequested) {
    require(!uploadStoreFile.isNullOrBlank()) { "ANDROID_UPLOAD_STORE_FILE is required for release signing" }
    require(!uploadStorePassword.isNullOrBlank()) { "ANDROID_UPLOAD_STORE_PASSWORD is required for release signing" }
    require(!uploadKeyAlias.isNullOrBlank()) { "ANDROID_UPLOAD_KEY_ALIAS is required for release signing" }
    require(!uploadKeyPassword.isNullOrBlank()) { "ANDROID_UPLOAD_KEY_PASSWORD is required for release signing" }
}

android {
    namespace = "com.example.woldeokmoneyverse"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.woldeok.moneyverse"
        minSdk = 21
        targetSdk = 36
        versionCode = 36
        versionName = "1.3.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = uploadStoreFile?.let { file(it) }
            storePassword = uploadStorePassword
            keyAlias = uploadKeyAlias
            keyPassword = uploadKeyPassword
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"https://test.easy-scraping.com/\"")
            buildConfigField("String", "API_HOST", "\"test.easy-scraping.com\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"https://easy-scraping.com/\"")
            buildConfigField("String", "API_HOST", "\"easy-scraping.com\"")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.material)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation("io.coil-kt:coil-compose:2.7.0")
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation("io.socket:socket.io-client:2.1.1") {
        exclude(group = "org.json", module = "json")
    }
    implementation(libs.kotlinx.coroutines)

    testImplementation(libs.junit)
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
