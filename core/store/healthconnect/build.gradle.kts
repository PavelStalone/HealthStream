plugins {
    id("android.library")
    id("android.compose")
    id("android.hilt")
    id("android.navigation")
}

android {
    namespace = "ru.health.stream.core.store.healthconnect"
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.health.connect.client)

    implementation(project(":core:store"))
    implementation(project(":core:monitor"))
    implementation(project(":core:starter"))
    implementation(project(":core:navigation"))

    implementation(project(":feature:settings"))
}
