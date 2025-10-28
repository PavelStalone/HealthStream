import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
import ru.health.stream.buildlogic.libs

class AndroidFeaturePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("health.stream.android.library")
                apply("health.stream.android.hilt")
            }

            dependencies {
                add("implementation", project(":core:monitor"))

                add("implementation", libs.findLibrary("kotlinx.coroutines.android").get())

                add("testImplementation", project(":core:test"))
                add("androidTestImplementation", project(":core:test"))
            }
        }
    }
}
