plugins {
    id("android.feature")
    id("android.compose")
}

android {
    namespace = "ru.health.stream.feature.onboarding.impl"
}

dependencies {
    implementation(projects.data.vitals)
    implementation(projects.data.report)

    implementation(projects.core.ui)

    implementation(projects.core.chart)
}
