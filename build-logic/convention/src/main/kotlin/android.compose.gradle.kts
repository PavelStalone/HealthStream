import ru.health.stream.buildlogic.extension.android
import ru.health.stream.buildlogic.extension.androidTestImplementation
import ru.health.stream.buildlogic.extension.debugImplementation
import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.libs

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    buildFeatures {
        compose = true
    }
}

dependencies {
    val bom = libs.androidx.compose.bom

    implementation(platform(bom))
    implementation(libs.bundles.health.stream.compose)

    androidTestImplementation(platform(bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
