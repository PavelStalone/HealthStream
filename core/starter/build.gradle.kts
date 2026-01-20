plugins {
    id("android.library")
    id("android.hilt")
}

android {
    namespace = "ru.health.stream.core.starter"
}

dependencies {
    api(libs.androidx.lifecycle.runtime.ktx)
}
