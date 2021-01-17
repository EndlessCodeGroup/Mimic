import ru.endlesscode.bukkitgradle.dependencies.aikar
import ru.endlesscode.bukkitgradle.dependencies.codemc
import ru.endlesscode.bukkitgradle.dependencies.spigotApi

plugins {
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("ru.endlesscode.bukkitgradle") version "0.9.1"
}

description = "Bukkit plugin with implementations of Mimic APIs"

bukkit {
    apiVersion = "1.16.4"

    meta {
        name.set("Mimic")
        apiVersion.set("1.13")
        authors.set(listOf("osipxd", "EndlessCodeGroup"))
        url.set("https://github.com/EndlessCodeGroup/Mimic")
    }

    server {
        setCore("paper")
        eula = true
    }
}

repositories {
    maven(url = "https://gitlab.com/endlesscodegroup/mvn-repo/raw/master/")
    maven(url = "https://mvn.lumine.io/repository/maven-public/")
    maven(url = "http://nexus.hc.to/content/repositories/pub_snapshots/")
    aikar()
    codemc()
    flatDir { dir("libs") }
}

dependencies {
    api(project(":mimic-bukkit-api"))

    compileOnly(spigotApi) { isTransitive = false }
    implementation(acf.paper)
    implementation(misc.bstats)

    compileOnly(rpgplugins.skillapi)
    compileOnly(rpgplugins.battlelevels)
    compileOnly(rpgplugins.mmoCore)
    compileOnly(rpgplugins.mmoLib)
    compileOnly(rpgplugins.mmoItems) { isTransitive = false }
    compileOnly(rpgplugins.heroes) { isTransitive = false }

    // From libs/ directory
    compileOnly(":CustomItemsAPI")
    compileOnly(":QuantumRPG:5.10.2")
    compileOnly(":NexEngine:2.0.3")

    testImplementation(spigotApi)
    testImplementation(rpgplugins.skillapi)
}

tasks.shadowJar {
    val shadePackage = "${project.group}.shade"
    relocate("co.aikar.commands", "$shadePackage.acf.commands")
    relocate("co.aikar.locales", "$shadePackage.acf.locales")
    relocate("kotlin", "$shadePackage.kotlin")
    relocate("org.intellij", "$shadePackage.intellij")
    relocate("org.jetbrains", "$shadePackage.jetbrains")
    relocate("org.bstats.bukkit", "$shadePackage.bstats")

    exclude("META-INF/*.kotlin_module")
    exclude("META-INF/maven/**")

    minimize()
}

