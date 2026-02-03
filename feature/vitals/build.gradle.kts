plugins {
    id("android.feature")
    id("android.compose")
}

android {
    namespace = "ru.health.stream.feature.vitals"
}

dependencies {
    api(project(":feature:vitals:data"))

    implementation(project(":core:starter"))

    implementation(project(":feature:vitals:source"))

    implementation(libs.io.github.automapper.annotation)

    ksp(libs.io.github.automapper.processor)
}
