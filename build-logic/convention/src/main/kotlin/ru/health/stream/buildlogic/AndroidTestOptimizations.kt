package ru.health.stream.buildlogic

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Project

/**
 * Constants defining standard test directories
 */
private const val UNIT_TEST_DIRECTORY = "src/test"
private const val ANDROID_TEST_DIRECTORY = "src/androidTest"

/**
 * Disable unnecessary unit tests for the [project] if there is no `test` folder.
 * Otherwise, these projects would be compiled and executed only to produce the following message:
 *
 * > SUCCESS: Executed 0 tests in XXms
 *
 * This optimization reduces build time by skipping test tasks when no tests are present.
 * The function checks for the existence of the standard unit test directory and disables
 * test variants if it's missing
 */
internal fun LibraryAndroidComponentsExtension.disableUnnecessaryUnitTests(project: Project) =
    beforeVariants { builder ->
        builder.hostTests.values.forEach { hostTestBuilder ->
            hostTestBuilder.enable = hostTestBuilder.enable
                    && project.projectDir.resolve(relative = UNIT_TEST_DIRECTORY).exists()
        }
    }

/**
 * Disable unnecessary Android instrumented tests for the [project] if there is no `androidTest` folder.
 * Otherwise, these projects would be compiled, packaged, installed and ran only to end-up with the following message:
 *
 * > Starting 0 tests on AVD
 *
 * Note: this could be improved by checking other potential sourceSets based on buildTypes and flavors
 */
internal fun LibraryAndroidComponentsExtension.disableUnnecessaryAndroidTests(project: Project) =
    beforeVariants { builder ->
        builder.androidTest.enable = builder.androidTest.enable
                && project.projectDir.resolve(relative = ANDROID_TEST_DIRECTORY).exists()
    }
