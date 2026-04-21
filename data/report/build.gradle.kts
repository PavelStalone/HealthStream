plugins {
    id("kotlin.library")
    id("kotlin.hilt")
}

dependencies {
    api(libs.kotlinx.datetime)

    implementation(projects.data.vitals)
    implementation(projects.data.personal)

    implementation(projects.core.common)
}
