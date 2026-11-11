// Root project build config

plugins {
    id("org.jetbrains.dokka")
    alias(libs.plugins.versions)
}

// Common configurations for all Mimic projects
subprojects {
    version = "0.9.0"
    group = "ru.endlesscode.mimic"
}

repositories {
    mavenCentral()
}

dependencies {
    dokka(projects.mimicApi)
    dokka(projects.mimicBukkitApi)
    dokka(projects.mimicBukkit)
}
