// Root project build config

plugins {
    base
}

// Common configurations for all Mimic projects
subprojects {
    configureProject()
}

// Configuration for API implementations
configure(subprojects - apiProject) {
    // Finish configuring MimicAPI first
    evaluationDependsOn(apiProject.path)

    // Use api as dependency
    dependencies {
        "api"(apiProject)
    }
}
