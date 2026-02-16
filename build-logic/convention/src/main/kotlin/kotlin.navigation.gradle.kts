import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.libs

plugins {
    id("kotlin.serialization")
}

dependencies {
    implementation(libs.androidx.navigation3.runtime)
}
