plugins {
    id("android.library")
    id("android.compose")
}

android {
    namespace = "ru.health.stream.feature.chart"
}

dependencies {

    implementation(projects.data.vitals)

    implementation(projects.core.common)
}
