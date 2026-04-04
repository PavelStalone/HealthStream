enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
        maven { setUrl("https://jitpack.io") }
    }
}

rootProject.name = "HealthStream"

include(":app")

// region Core module
include(":core:ui")
include(":core:test")
include(":core:store")
include(":core:store:room")
include(":core:store:datastore")
include(":core:store:healthconnect")
include(":core:common")
include(":core:starter")
include(":core:monitor")
include(":core:monitor:timber")
include(":core:navigation")
include(":core:communication:ble")
include(":core:communication:ble:lib")
include(":core:communication:ktor")
// endregion

// region Feature module
include(":feature:chart")
include(":feature:vitals")
include(":feature:vitals:data")
include(":feature:vitals:source")
include(":feature:settings")
include(":feature:personal")
include(":feature:personal:data")
include(":feature:personal:source")
// endregion
