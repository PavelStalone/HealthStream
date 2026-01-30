import ru.health.stream.buildlogic.extension.implementation

plugins {
    id("com.android.library")
    id("kotlin.android")
    id("org.gradle.android.cache-fix")
}

dependencies {
    implementation(project(":core:common"))
}
