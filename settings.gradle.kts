pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { mavenCentral() }
    versionCatalogs {
        create("libs") {
            // ✅ 오직 한 번만!
//            from(files("gradle/libs.versions.toml"))
        }
    }
}


rootProject.name = "FinSight"


include(
    ":apps:api",
//    ":apps:ml-forecast", // not a Gradle project (Python) — but kept for tree clarity
    ":libs:common-core",
    ":libs:common-domain",
    ":libs:common-infra",
)