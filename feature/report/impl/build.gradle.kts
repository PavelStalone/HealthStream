plugins {
    id("android.feature")
    id("android.compose")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.feature.report.impl"
}

dependencies {
    api(projects.feature.report.api)

    implementation(projects.data.vitals)
    implementation(projects.data.report)

    implementation(projects.core.ui)

    implementation(projects.feature.chart)
}
