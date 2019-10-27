// Bukkit MimicAPI implementation build config

plugins {
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("ru.endlesscode.bukkitgradle")
}

val minorVersion = 0

version = "${apiProject.version}.$minorVersion"
description = "API for integration with Bukkit RPG plugins"

bukkit {
    version = "1.12.2"

    meta {
        setName("BukkitMimic")
        setMain("$group.$name")
        setAuthors(listOf("osipxd", "EndlessCodeGroup"))
        setUrl("https://github.com/EndlessCodeGroup/BukkitMimic")
    }

    run {
        setCore("paper")
        eula = true
    }
}

repositories {
    maven(url = "https://raw.github.com/EndlessCodeGroup/mvn-repo/master/")
    maven(url = "https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    compileOnly(bukkit)
    implementation(deps.acf)

    compileOnly(
        deps.skillApi,
        deps.battleLevelsApi
    )
}

tasks.shadowJar {
    relocate("co.aikar.commands", "${group}.shade.acf")
}

tasks.build.get().dependsOn(tasks.shadowJar)
