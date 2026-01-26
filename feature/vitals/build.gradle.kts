plugins {
    id("android.feature")
    id("android.compose")
}

android {
    namespace = "ru.health.stream.feature.vitals"
}

dependencies {
    api(project(":feature:vitals:data"))

    implementation(project(":feature:vitals:source"))
}
