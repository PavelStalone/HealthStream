import ru.health.stream.buildlogic.extension.androidTestImplementation
import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.ksp
import ru.health.stream.buildlogic.extension.kspAndroidTest
import ru.health.stream.buildlogic.extension.kspTest
import ru.health.stream.buildlogic.extension.libs
import ru.health.stream.buildlogic.extension.testImplementation

plugins {
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

dependencies {
    ksp(libs.androidx.hilt.ext.compiler)
    ksp(libs.google.dagger.hilt.android.compiler)

    implementation(libs.androidx.hilt.ext.work)
    implementation(libs.androidx.hilt.ext.common)
    implementation(libs.google.dagger.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)

    // Testing
    kspTest(libs.google.dagger.hilt.android.testing)
    testImplementation(libs.google.dagger.hilt.android.testing)

    // Android testing
    kspAndroidTest(libs.google.dagger.hilt.android.testing)
    androidTestImplementation(libs.google.dagger.hilt.android.testing)
}
