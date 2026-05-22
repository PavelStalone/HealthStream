plugins {
    id("kotlin.library")
    id("kotlin.hilt")
}

dependencies {
    implementation(projects.core.common)

    api(libs.bundles.health.stream.test)
}
