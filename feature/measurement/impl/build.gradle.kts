plugins {
    id("android.feature")
    id("android.compose")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.feature.measurement.impl"
}

dependencies {
    api(projects.feature.measurement.api)

    implementation(projects.data.vitals)

    implementation(projects.core.ui)

    implementation(projects.core.chart)

    implementation(libs.lottie.compose)
}
