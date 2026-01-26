import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.ksp
import ru.health.stream.buildlogic.extension.libs

plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    ksp(libs.google.dagger.compiler)

    implementation(libs.google.dagger)
}
