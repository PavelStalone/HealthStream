pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HealthStream"

include(":app")

// region Core module
include(":ui")
include(":test")
include(":common")
include(":starter")
project(":ui").projectDir = file("core/ui")
project(":test").projectDir = file("core/test")
project(":common").projectDir = file("core/common")
project(":starter").projectDir = file("core/starter")

include(":monitor")
include(":monitor:timber")
project(":monitor").projectDir = file("core/monitor")
project(":monitor:timber").projectDir = file("core/monitor/timber")

include(":store")
include(":store:room")
include(":store:datastore")
include(":store:healthconnect")
project(":store").projectDir = file("core/store")
project(":store:room").projectDir = file("core/store/room")
project(":store:datastore").projectDir = file("core/store/datastore")
project(":store:healthconnect").projectDir = file("core/store/healthconnect")

include(":ble")
include(":ktor")
include(":ble:lib")
project(":ble").projectDir = file("core/communication/ble")
project(":ktor").projectDir = file("core/communication/ktor")
project(":ble:lib").projectDir = file("core/communication/ble/lib")
// endregion

// region Feature module
include(":chart")
include(":vitals")
include(":settings")
include(":vitals:data")
include(":vitals:source")
project(":chart").projectDir = file("feature/chart")
project(":vitals").projectDir = file("feature/vitals")
project(":settings").projectDir = file("feature/settings")
project(":vitals:data").projectDir = file("feature/vitals/data")
project(":vitals:source").projectDir = file("feature/vitals/source")
// endregion
