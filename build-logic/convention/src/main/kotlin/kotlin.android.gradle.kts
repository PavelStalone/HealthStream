import ru.health.stream.buildlogic.extension.android
import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.libs

android {
    compileSdk {
        version = release(version = libs.versions.compileSdk.get().toInt())
    }

    defaultConfig.apply {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
