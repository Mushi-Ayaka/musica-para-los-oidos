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
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "MusicaParaLosOidos"
include(":app")
include(":core:model")
include(":core:data")
include(":core:playback")
include(":core:designsystem")
include(":feature:library")
include(":feature:player")
include(":feature:equalizer")
include(":feature:settings")
include(":feature:downloader")
