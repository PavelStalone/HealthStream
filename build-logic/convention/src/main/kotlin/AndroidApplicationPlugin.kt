import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import ru.health.stream.buildlogic.configureAndroidCompose
import ru.health.stream.buildlogic.configureKotlinAndroid
import ru.health.stream.buildlogic.libs
import java.io.File

class AndroidApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("org.gradle.android.cache-fix")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                configureAndroidCompose(this)

                defaultConfig {
                    targetSdk = libs.findVersion("targetSdk").get().toString().toInt()
                    multiDexEnabled = true
                }

                buildFeatures {
                    buildConfig = true
                }
            }

            dependencies {
                add("implementation", libs.findLibrary("kotlinx.coroutines.android").get())
                add("implementation", libs.findLibrary("kotlinx.datetime").get())
            }
        }
    }
}
