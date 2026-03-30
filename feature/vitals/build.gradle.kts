plugins {
    id("android.feature")
    id("android.compose")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.feature.vitals"
}

dependencies {
    api(projects.feature.vitals.data)

    implementation(projects.core.ui)
    implementation(projects.core.starter)
    implementation(projects.core.navigation)

    implementation(projects.feature.chart)
    implementation(projects.feature.settings)
    implementation(projects.feature.vitals.source)

    implementation(libs.io.github.automapper.annotation)
    ksp(libs.io.github.automapper.processor)
}
