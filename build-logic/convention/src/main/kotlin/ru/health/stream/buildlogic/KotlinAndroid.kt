package ru.health.stream.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = libs.findVersion("compileSdk").get().toString().toInt()

        defaultConfig {
            minSdk = libs.findVersion("minSdk").get().toString().toInt()
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            val javaVersion = libs.findVersion("java")
                .get()
                .toString()
                .toInt()
                .let { java -> JavaVersion.toVersion(java) }

            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
    }
}
