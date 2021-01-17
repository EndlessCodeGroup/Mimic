// Root project build config

plugins {
    id("com.github.ben-manes.versions") version "0.36.0"
}

// Common configurations for all Mimic projects
subprojects {
    version = "0.6-SNAPSHOT"
    group = "ru.endlesscode.mimic"

    configureProject()
    configureBintray(
        repoUrl = "https://github.com/EndlessCodeGroup/Mimic",
        projectName = "Mimic"
    )
}
