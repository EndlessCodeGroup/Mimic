// Bukkit MimicAPI implementation build config

plugins {
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("ru.endlesscode.bukkitgradle")
}

val minorVersion = 0

version = "${apiProject.version}.$minorVersion"
description = "API for integration with Bukkit RPG plugins"

bukkit {
    version = "1.14.4"

    meta {
        setName("BukkitMimic")
        setMain("$group.$name")
        setAuthors(listOf("osipxd", "EndlessCodeGroup"))
        setUrl("https://github.com/EndlessCodeGroup/BukkitMimic")
    }

    run {
        setCore("bukkit")
        eula = true
    }
}

repositories {
    maven(url = "https://gitlab.com/endlesscodegroup/mvn-repo/raw/master/")
    maven(url = "https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    compileOnly(bukkit) { isTransitive = false }
    implementation(deps.acf)

    compileOnly(deps.skillApi)
    compileOnly(deps.battleLevelsApi)

    testImplementation(bukkit)
    testImplementation(deps.skillApi)
}

tasks.shadowJar {
    relocate("co.aikar.commands", "${group}.shade.acf")
}

tasks.build.get().dependsOn(tasks.shadowJar)
