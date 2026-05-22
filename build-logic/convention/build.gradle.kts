plugins {
    `kotlin-dsl`
}

group = "ru.health.stream.buildlogic"

dependencies {
    implementation(libs.gradlePlugin.ksp)
    implementation(libs.gradlePlugin.hilt)
    implementation(libs.gradlePlugin.room)
    implementation(libs.gradlePlugin.kover)
    implementation(libs.gradlePlugin.kotlin)
    implementation(libs.gradlePlugin.android)
    implementation(libs.gradlePlugin.compose)
    implementation(libs.gradlePlugin.cache.fix)
    implementation(libs.gradlePlugin.kotlin.serialization)

    implementation(files(libs::class.java.superclass.protectionDomain.codeSource.location))
}
