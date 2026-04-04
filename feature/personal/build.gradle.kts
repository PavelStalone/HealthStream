plugins {
    id("android.feature")
    id("android.compose")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.feature.personal"
}

dependencies {
    api(projects.feature.personal.data)

    implementation(projects.core.ui)
    implementation(projects.core.starter)
    implementation(projects.core.navigation)

    implementation(projects.feature.settings)
    implementation(projects.feature.personal.source)

    implementation(libs.io.github.automapper.annotation)

    ksp(libs.io.github.automapper.processor)
}
