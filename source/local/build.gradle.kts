plugins {
    id("kotlin.library")
    id("kotlin.hilt")
}

tasks.withType<Test> {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

dependencies {
    api(projects.source)

    implementation(projects.core.common)

    testImplementation(projects.core.test)
}
