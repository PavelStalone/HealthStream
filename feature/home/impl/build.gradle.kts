plugins {
    id("android.feature")
    id("android.compose")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.feature.home.impl"
}

dependencies {
    api(projects.feature.home.api)

    implementation(projects.data.vitals)

    implementation(projects.core.ui)

    implementation(projects.feature.chart)
    implementation(projects.feature.report.api)
    implementation(projects.feature.measurement.api)
}
