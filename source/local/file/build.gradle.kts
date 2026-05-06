plugins {
    id("android.library")
    id("android.compose")
    id("android.hilt")
}

android {
    namespace = "ru.health.stream.source.local.file"
}

dependencies {
    implementation(projects.source.local)

    implementation(projects.data.report)
    implementation(projects.data.vitals)
    implementation(projects.data.personal)

    implementation(projects.core.ui)
    implementation(projects.core.common)
    implementation(projects.core.monitor)
    implementation(projects.core.starter)

    implementation(projects.feature.chart)

    implementation(libs.itext7.core)
}
