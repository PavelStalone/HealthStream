plugins {
    id("android.feature")
    id("android.compose")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.feature.onboarding.impl"
}

dependencies {
    api(projects.feature.onboarding.api)

    implementation(projects.data.vitals)
    implementation(projects.data.report)
    implementation(projects.data.setting)

    implementation(projects.core.ui)
    implementation(projects.core.chart)

    implementation(projects.feature.user.api)
}
