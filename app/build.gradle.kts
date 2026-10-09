
plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.thaisuboverlay"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.thaisuboverlay"
        minSdk = 26
        targetSdk = 35

        versionCode = 2
        versionName = "0.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("com.google.mlkit:translate:17.0.3")
}

