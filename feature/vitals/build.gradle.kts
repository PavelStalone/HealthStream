plugins {
    id("android.feature")
    id("android.compose")
}

android {
    namespace = "ru.health.stream.feature.vitals"
}

dependencies {
    api(project(":vitals:data"))

    implementation(project(":vitals:source"))
}
