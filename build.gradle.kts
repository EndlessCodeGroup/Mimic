// Root project build config

plugins {
    id("com.github.ben-manes.versions") version "0.27.0"
}

// Common configurations for all Mimic projects
subprojects {
    configureProject()

    version = "0.2"
    group = "ru.endlesscode.mimic"
}
