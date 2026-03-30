import ru.health.stream.buildlogic.extension.implementation
import ru.health.stream.buildlogic.extension.libs

plugins {
    id("com.google.devtools.ksp")
    id("androidx.room")
}

room {
    schemaDirectory(path = "$projectDir/schemas")
}

dependencies {
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
}
