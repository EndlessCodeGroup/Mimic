description = "Abstraction API for Bukkit RPG plugins"

repositories {
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots")
}

dependencies {
    api(project(":mimic-api"))
    compileOnly("org.jetbrains:annotations:23.0.0")
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT") { isTransitive = false }
}
