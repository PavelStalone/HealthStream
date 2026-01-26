import ru.health.stream.buildlogic.extension.libs

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
