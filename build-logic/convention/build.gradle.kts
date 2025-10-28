plugins {
    `kotlin-dsl`
}

group = "ru.health.stream.buildlogic"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    compileOnly(libs.gradlePlugin.ksp)
    compileOnly(libs.gradlePlugin.kotlin)
    compileOnly(libs.gradlePlugin.android)
    compileOnly(libs.gradlePlugin.compose)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "health.stream.android.library"
            implementationClass = "AndroidLibraryPlugin"
        }
        register("androidLibraryCompose") {
            id = "health.stream.android.library.compose"
            implementationClass = "AndroidLibraryComposePlugin"
        }
        register("androidHilt") {
            id = "health.stream.android.hilt"
            implementationClass = "AndroidHiltPlugin"
        }
        register("androidFeature") {
            id = "health.stream.android.feature"
            implementationClass = "AndroidFeaturePlugin"
        }
        register("androidApplication") {
            id = "health.stream.android.application"
            implementationClass = "AndroidApplicationPlugin"
        }
    }
}
