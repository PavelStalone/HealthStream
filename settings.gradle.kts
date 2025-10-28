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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HealthStream"

include(":app")

// region Core module
include(":core:ui")
include(":core:store")
include(":core:common")
include(":core:store:datastore")
include(":core:communication:ble")
include(":core:communication:ktor")
// endregion

include(":core:test")
include(":core:monitor")
