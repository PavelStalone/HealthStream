plugins {
    id("health.stream.android.feature")
    id("health.stream.android.library.compose")
}

android {
    namespace = "ru.health.stream.feature.vitals"
}

dependencies{
    api(project(":feature:vitals:data"))

    implementation(project(":feature:vitals:source"))
}