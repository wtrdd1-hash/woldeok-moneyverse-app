plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.woldeokmoneyverse"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.woldeok.moneyverse"
        minSdk = 21
        targetSdk = 36
        versionCode = 29
        versionName = "1.2.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("${rootDir}/woldeok-release-key.jks")
            storePassword = System.getenv("ANDROID_UPLOAD_STORE_PASSWORD") ?: "woldeok1234"
            keyAlias = System.getenv("ANDROID_UPLOAD_KEY_ALIAS") ?: "woldeok-key"
            keyPassword = System.getenv("ANDROID_UPLOAD_KEY_PASSWORD") ?: "woldeok1234"
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"https://test.easy-scraping.com/\"")
            buildConfigField("String", "API_HOST", "\"test.easy-scraping.com\"")
            signingConfig = signingConfigs.getByName("release")
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
