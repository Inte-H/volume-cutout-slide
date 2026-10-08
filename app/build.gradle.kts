plugins {
    id("com.android.application")
}

android {
    namespace = "io.github.hit.volcutout"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.hit.volcutout"
        minSdk = 30
        targetSdk = 36
        versionCode = 4
        versionName = "1.3"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
}
