plugins {
    id("com.android.application")
}

android {
    namespace = "io.github.inte_h.volcutout"
    compileSdk {
        version = release(37) { minorApiLevel = 1 }
    }

    defaultConfig {
        applicationId = "io.github.inte_h.volcutout"
        minSdk = 30
        targetSdk = 36
        versionCode = 5
        versionName = "2.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    compileOnly("io.github.libxposed:api:102.0.0")
    implementation("io.github.libxposed:service:102.0.0")
}
