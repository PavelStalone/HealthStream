plugins {
    id("kotlin.library")
    id("kotlin.hilt")
}

dependencies {
    api(projects.data.vitals)
    api(projects.data.report)
    api(projects.data.setting)
    api(projects.data.personal)

    implementation(projects.core.common)
}
