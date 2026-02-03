import ru.health.stream.buildlogic.extension.androidTestImplementation
import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.testImplementation

plugins {
    id("android.library")
    id("android.hilt")
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:monitor"))

    testImplementation(project(":core:test"))
    androidTestImplementation(project(":core:test"))
}
