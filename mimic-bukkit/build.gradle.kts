import ru.endlesscode.bukkitgradle.dependencies.aikar
import ru.endlesscode.bukkitgradle.dependencies.codemc

plugins {
    commons
    publish
    alias(libs.plugins.shadow)
    alias(libs.plugins.bukkitgradle)
    kotlin("plugin.serialization")
}

description = "Bukkit plugin with implementations of Mimic APIs"

bukkit {
    meta {
        name = "Mimic"
        main = "ru.endlesscode.mimic.MimicPlugin"
        apiVersion = "1.13"
        authors = listOf("osipxd", "EndlessCodeGroup")
        url = "https://github.com/EndlessCodeGroup/Mimic"
    }

    server {
        setCore("paper")
        eula = true
    }
}

repositories {
    maven(url = "https://gitlab.com/endlesscodegroup/mvn-repo/raw/master/")
    maven(url = "https://mvn.lumine.io/repository/maven-public/") {
        content {
            includeModule("me.robin", "BattleLevels")
            includeGroup("net.Indyuce")
            includeModule("io.lumine", "MythicLib-dist")
        }
    }
    aikar()
    codemc()
    // Uncomment if you want to get Heroes from maven repo
    //maven(url = "https://nexus.hc.to/content/repositories/pub_releases/")
    flatDir { dir("libs") }
}

dependencies {
    api(project(":mimic-bukkit-api"))

    compileOnly(libs.spigot.api) { isTransitive = false }
    compileOnly(libs.annotations)

    implementation(libs.bstats)
    implementation(libs.serialization.hocon)
    implementation(libs.commandapi)
    implementation(libs.commandapi.kotlin)
    implementation(libs.adventure)

    compileOnly(libs.bundles.rpgplugins) { isTransitive = false }

    // From libs/ directory
    compileOnly(":CustomItemsAPI")
    compileOnly(":QuantumRPG:5.10.2")
    compileOnly(":NexEngine:2.0.3") // Do not update NexEngine. QuantumRpgWrapper cannot compile with higher version

    testImplementation(libs.spigot.api)
    testImplementation(libs.rpgplugins.skillapi)
}

kotlin {
    compilerOptions {
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}

tasks.shadowJar {
    dependencies {
        exclude(dependency("org.jetbrains:annotations:.*"))
    }

    val shadePackage = "${project.group}.shade"
    relocate("kotlin", "$shadePackage.kotlin")
    relocate("org.bstats", "$shadePackage.bstats")
    relocate("com.typesafe.config", "$shadePackage.hocon")
    relocate("dev.jorel.commandapi", "$shadePackage.commandapi")
    relocate("net.kyori.adventure", "$shadePackage.adventure")
    relocate("net.kyori.examination", "$shadePackage.examination")

    exclude("META-INF/*.kotlin_module")
    exclude("META-INF/com.android.tools/**")
    exclude("META-INF/proguard/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/**/module-info.class")
    exclude("LICENSE")

    minimize()
}
