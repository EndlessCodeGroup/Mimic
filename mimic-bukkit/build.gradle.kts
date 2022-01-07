import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import ru.endlesscode.bukkitgradle.dependencies.aikar
import ru.endlesscode.bukkitgradle.dependencies.codemc
import ru.endlesscode.bukkitgradle.dependencies.spigotApi

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("ru.endlesscode.bukkitgradle") version "0.10.0"
    kotlin("plugin.serialization")
}

description = "Bukkit plugin with implementations of Mimic APIs"

bukkit {
    apiVersion = "1.17.1"

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
    aikar()
    codemc()
    flatDir { dir("libs") }
    // Uncomment if you want to get Heroes from maven repo
    //maven(url = "https://nexus.hc.to/content/repositories/pub_snapshots/")
}

dependencies {
    api(project(":mimic-bukkit-api"))

    compileOnly(spigotApi) { isTransitive = false }
    compileOnly(misc.annotations)

    implementation(acf.paper)
    implementation(misc.bstats)
    implementation(misc.serialization_hocon)

    compileOnly(rpgplugins.skillapi)
    compileOnly(rpgplugins.battlelevels)
    compileOnly(rpgplugins.mmoCore)
    compileOnly(rpgplugins.mythicLib)
    compileOnly(rpgplugins.mmoItems) { isTransitive = false }
    compileOnly(rpgplugins.heroes) { isTransitive = false }

    // From libs/ directory
    compileOnly(":CustomItemsAPI")
    compileOnly(":QuantumRPG:5.10.2")
    compileOnly(":NexEngine:2.0.3")

    testImplementation(spigotApi)
    testImplementation(rpgplugins.skillapi)
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
}

tasks.shadowJar {
    dependencies {
        exclude(dependency(misc.annotations))
    }

    val shadePackage = "${project.group}.shade"
    relocate("co.aikar.commands", "$shadePackage.acf.commands")
    relocate("co.aikar.locales", "$shadePackage.acf.locales")
    relocate("kotlin", "$shadePackage.kotlin")
    relocate("org.bstats.bukkit", "$shadePackage.bstats")
    relocate("com.typesafe.config", "$shadePackage.hocon")

    exclude("META-INF/*.kotlin_module")
    exclude("META-INF/maven/**")

    minimize()
}
