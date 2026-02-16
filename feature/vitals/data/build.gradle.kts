plugins {
    id("kotlin.library")
    id("kotlin.navigation")
    id("com.google.devtools.ksp")
}

dependencies {
    api(libs.kotlinx.datetime)

    implementation(libs.io.github.automapper.annotation)

    ksp(libs.io.github.automapper.processor)
}
