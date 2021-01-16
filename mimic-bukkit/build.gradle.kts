import ru.endlesscode.bukkitgradle.dependencies.*

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
        url.set("https://github.com/EndlessCodeGroup/MimicAPI")
    }

    server {
        setCore("paper")
        eula = true
    }
}

repositories {
    maven(url = "https://gitlab.com/endlesscodegroup/mvn-repo/raw/master/")
    aikar()
    codemc()
    flatDir { dir("libs") }
}

dependencies {
    api(project(":mimic-bukkit-api"))

    compileOnly(bukkitApi) { isTransitive = false }
    implementation(deps.acf)
    implementation(deps.bstats_bukkit)

    compileOnly(deps.skillapi)
    compileOnly(deps.battlelevels)

    // From libs/ directory
    compileOnly(":CustomItemsAPI")

    testImplementation(bukkitApi)
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
