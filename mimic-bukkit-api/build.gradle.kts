plugins {
    commons
    publish
}

description = "Abstraction API for Bukkit RPG plugins"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api(projects.mimicApi)
    compileOnly(libs.annotations)
    compileOnly(libs.paperApi)
}
