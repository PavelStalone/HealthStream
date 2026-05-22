plugins {
    id("kotlin.library")
    id("kotlin.hilt")
}

tasks.withType<Test> {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

dependencies {
    api(libs.kotlinx.datetime)

    implementation(projects.data.vitals)
    implementation(projects.data.personal)

    implementation(projects.core.common)

    testImplementation(projects.core.test)
}
