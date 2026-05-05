plugins {
    id("android.feature")
    id("android.compose")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.feature.onboarding.impl"
}

dependencies {
    implementation(projects.core.starter)

    implementation(projects.data.vitals)
    implementation(projects.data.report)

    implementation(projects.core.ui)

    implementation(projects.feature.chart)
    implementation(projects.feature.user.api)

    implementation(libs.lottie.compose)
}
