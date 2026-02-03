plugins {
    id("kotlin.library")
    id("com.google.devtools.ksp")
}

dependencies {
    api(libs.kotlinx.datetime)

    implementation(libs.io.github.automapper.annotation)

    ksp(libs.io.github.automapper.processor)
}
