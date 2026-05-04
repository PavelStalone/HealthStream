plugins {
    id("kotlin.library")
    id("kotlin.navigation")
}

dependencies {
    implementation(projects.data.vitals)

    implementation(projects.core.common)
}
