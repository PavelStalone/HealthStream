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

//region Core module
include(":core:ui")
include(":core:test")
include(":core:common")
include(":core:starter")
include(":core:monitor")
include(":core:monitor:timber")
include(":core:navigation")
include(":core:store:datastore")
include(":core:store:healthconnect")
//endregion

//region Data module
include(":data:vitals")
include(":data:report")
include(":data:personal")
//endregion

//region Feature module
include(":feature:chart")
include(":feature:settings")
include(":feature:user:api")
include(":feature:user:impl")
include(":feature:home:api")
include(":feature:home:impl")
include(":feature:report:api")
include(":feature:report:impl")
include(":feature:measurement:api")
include(":feature:measurement:impl")
//endregion

//region Source module
include(":source:local")
include(":source:local:room")
include(":source:local:healthconnect")
include(":source:remote:ble")
include(":source:remote:ble:lib")
//endregion
