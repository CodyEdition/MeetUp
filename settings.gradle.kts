import groovy.json.JsonOutput
import java.io.File

pluginManagement {
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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Meet Up"
include(":app")

val __agentLogFile = File(settings.rootDir, "debug-f7e46a.log")
fun __agentNdjson(hypothesisId: String, message: String, data: Map<String, Any?>) {
    __agentLogFile.appendText(
        JsonOutput.toJson(
            mapOf(
                "sessionId" to "f7e46a",
                "hypothesisId" to hypothesisId,
                "location" to "settings.gradle.kts",
                "message" to message,
                "data" to data,
                "timestamp" to System.currentTimeMillis(),
            ),
        ) + "\n",
    )
}
__agentNdjson(
    "H1",
    "JVM hosting Gradle (java.home)",
    mapOf("javaHome" to (System.getProperty("java.home") ?: "")),
)
__agentNdjson(
    "H2",
    "Env and Gradle JDK property at settings time",
    mapOf(
        "envJAVA_HOME" to (System.getenv("JAVA_HOME") ?: ""),
        "gradlePropertyOrgGradleJavaHome" to (settings.providers.gradleProperty("org.gradle.java.home").orNull ?: "unset"),
    ),
)
gradle.projectsLoaded {
    __agentNdjson(
        "H4",
        "java.home after projects loaded (daemon context)",
        mapOf("javaHome" to (System.getProperty("java.home") ?: "")),
    )
}