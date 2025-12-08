plugins {
    id("health.stream.android.library")
}

android {
    namespace = "ru.health.stream.feature.vitals.source"
}

dependencies {
    api(project(":feature:vitals:data"))

    implementation(libs.kotlinx.datetime)
}
