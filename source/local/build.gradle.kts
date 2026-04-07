plugins {
    id("kotlin.library")
    id("kotlin.hilt")
}

dependencies {
    api(projects.data.vitals)
    api(projects.data.personal)
}
