// Root project build config

plugins {
    id("com.github.ben-manes.versions") version "0.29.0"
}

// Common configurations for all Mimic projects
subprojects {
    version = "0.4"
    group = "ru.endlesscode.mimic"

    configureProject()
    configureBintray(
        repoUrl = "https://github.com/EndlessCodeGroup/MimicAPI",
        projectName = "Mimic"
    )
}
