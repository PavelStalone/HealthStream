import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import ru.health.stream.buildlogic.extension.java
import ru.health.stream.buildlogic.extension.libs
import ru.health.stream.buildlogic.extension.projectJavaVersion

plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = projectJavaVersion
    targetCompatibility = projectJavaVersion
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(libs.versions.java.get())
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
