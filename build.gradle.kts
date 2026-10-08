import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
    id("com.android.application") version "9.4.1" apply false
}

val keystoreFile = rootProject.file("keystore.properties")

subprojects {
    plugins.withId("com.android.application") {
        if (!keystoreFile.exists()) return@withId
        val props = Properties().apply { keystoreFile.inputStream().use { load(it) } }
        extensions.configure<ApplicationExtension> {
            val local = signingConfigs.create("local") {
                storeFile = rootProject.file(props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
            buildTypes.getByName("debug").signingConfig = local
            buildTypes.getByName("release").signingConfig = local
        }
    }
}
