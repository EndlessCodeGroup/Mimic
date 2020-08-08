// Bukkit MimicAPI implementation build config

plugins {
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("ru.endlesscode.bukkitgradle")
}

description = "Bukkit plugin with implementations of Mimic APIs"

bukkit {
    version = "1.16.1"

    meta {
        setName("Mimic")
        setMain("$group.$name")
        setAuthors(listOf("osipxd", "EndlessCodeGroup"))
        setUrl("https://github.com/EndlessCodeGroup/MimicAPI")
    }

    run {
        setCore("spigot")
        eula = true
    }
}

repositories {
    maven(url = "https://gitlab.com/endlesscodegroup/mvn-repo/raw/master/")
    maven(url = "https://repo.aikar.co/content/groups/aikar/")
    maven(url = "https://repo.codemc.org/repository/maven-public")
    flatDir { dir("libs") }
}

dependencies {
    api(project(":mimic-bukkit-api"))

    compileOnly(bukkit) { isTransitive = false }
    implementation(deps.acf)
    implementation(deps.bstats_bukkit)

    compileOnly(deps.skillapi)
    compileOnly(deps.battlelevels)

    // From libs/ directory
    compileOnly(":CustomItemsAPI")

    testImplementation(bukkit)
    testImplementation(deps.skillapi)
}

tasks.shadowJar {
    val shadePackage = "${project.group}.shade"
    relocate("co.aikar", "$shadePackage.acf")
    relocate("kotlin", "$shadePackage.kotlin")
    relocate("org.intellij", "$shadePackage.intellij")
    relocate("org.jetbrains", "$shadePackage.jetbrains")
    relocate("org.bstats.bukkit", "$shadePackage.bstats")

    exclude("META-INF/*.kotlin_module")
    exclude("META-INF/maven/**")

    minimize()
}

tasks.named("assemble") {
    dependsOn(tasks.shadowJar)
}
