description = "Abstraction API for Bukkit RPG plugins"

repositories {
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots")
}

dependencies {
    api(project(":mimic-api"))
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT") { isTransitive = false }
}
