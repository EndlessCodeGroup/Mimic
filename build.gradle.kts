// Root project build config

plugins {
    id("org.jetbrains.dokka")
    alias(libs.plugins.versions)
}

// Common configurations for all Mimic projects
subprojects {
    version = "0.9.0-SNAPSHOT"
    group = "ru.endlesscode.mimic"
}

repositories {
    mavenCentral()
}
