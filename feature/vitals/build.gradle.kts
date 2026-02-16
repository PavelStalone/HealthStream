plugins {
    id("android.feature")
    id("android.compose")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.feature.vitals"
}

dependencies {
    api(project(":feature:vitals:data"))

    implementation(project(":core:ui"))
    implementation(project(":core:starter"))

    implementation(project(":feature:chart"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:vitals:source"))

    implementation(libs.io.github.automapper.annotation)

    ksp(libs.io.github.automapper.processor)
}
