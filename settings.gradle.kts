@file:Suppress("UnstableApiUsage")

include(":test")


pluginManagement {
    repositories {
        google()
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

rootProject.name = "GradleKnife"
include(":app")
include(":knife-sdk")
includeBuild("./knife-plugin")