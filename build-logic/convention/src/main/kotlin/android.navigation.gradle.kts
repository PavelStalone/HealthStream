import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.libs

plugins {
    id("kotlin.navigation")
}

dependencies {
    implementation(libs.navigation.router)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
}
