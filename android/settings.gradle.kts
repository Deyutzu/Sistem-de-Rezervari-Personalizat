// De unde descarca Gradle plugin-urile si librariile.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Toate repository-urile sunt declarate DOAR aici (nu si in module) - configurare centralizata
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "StayFinder"
include(":app")
