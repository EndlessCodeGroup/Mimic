enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
}

rootProject.name = "mimic"

include(
    ":mimic-api",
    ":mimic-bukkit-api", ":mimic-bukkit"
)
