plugins {
    commons
    publish
}

description = "Abstraction API for Bukkit RPG plugins"

repositories {
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots")
}

dependencies {
    api(projects.mimicApi)
    compileOnly(libs.annotations)
    compileOnly(libs.spigot.api) { isTransitive = false }
}
