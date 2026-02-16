import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.ksp
import ru.health.stream.buildlogic.extension.libs

plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    ksp(libs.dagger.hilt.compiler)
    ksp(libs.androidx.hilt.ext.compiler)

    implementation(libs.dagger.hilt.core)
}
