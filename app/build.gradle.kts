plugins { id("com.android.application") }
android { namespace = "com.example.thaisuboverlay"; compileSdk = 35
 defaultConfig { applicationId = "com.example.thaisuboverlay"; minSdk = 26; targetSdk = 32; versionCode = 1; versionName = "0.1" } }
dependencies { implementation("com.google.mlkit:translate:17.0.3") }
