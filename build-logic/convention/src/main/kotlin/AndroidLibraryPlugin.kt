import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import ru.health.stream.buildlogic.configureKotlinAndroid
import ru.health.stream.buildlogic.disableUnnecessaryAndroidTests
import ru.health.stream.buildlogic.disableUnnecessaryUnitTests
import ru.health.stream.buildlogic.libs

class AndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("org.gradle.android.cache-fix")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
            }

            extensions.configure<LibraryAndroidComponentsExtension> {
                disableUnnecessaryUnitTests(target)
                disableUnnecessaryAndroidTests(target)
            }

            dependencies {
                add("testImplementation", kotlin("test"))
                add("androidTestImplementation", kotlin("test"))
            }
        }
    }
}
