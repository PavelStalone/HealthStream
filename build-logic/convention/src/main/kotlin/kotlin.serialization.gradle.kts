import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.libs

plugins {
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
