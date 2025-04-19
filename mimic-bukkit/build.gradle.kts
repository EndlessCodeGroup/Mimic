import ru.endlesscode.bukkitgradle.dependencies.aikar
import ru.endlesscode.bukkitgradle.dependencies.codemc
import ru.endlesscode.bukkitgradle.dependencies.papermc

plugins {
    commons
    publish
    alias(libs.plugins.shadow)
    alias(libs.plugins.bukkitgradle)
    kotlin("plugin.serialization")
}

description = "Bukkit plugin implementing Mimic APIs"

bukkit {
    apiVersion = "1.20"

    plugin {
        name = "Mimic"
        main = "ru.endlesscode.mimic.MimicPlugin"
        authors = listOf("osipxd", "EndlessCodeGroup")
        website = "https://github.com/EndlessCodeGroup/Mimic"
        softDepend = listOf("CommandAPI")
        loadBefore = listOf(
            "SkillAPI",
            "BattleLevels",
            "CustomItems",
            "MMOCore",
            "MMOItems",
            "Heroes",
            "QuantumRPG",
        )
    }

    server {
        version = "1.21.5"
        eula = true
    }
}

tasks.runServer {
    downloadPlugins {
        github("CommandAPI", "CommandAPI", "10.0.0", "CommandAPI-10.0.0.jar")
    }
}

repositories {
    papermc()
    maven(url = "https://gitlab.com/endlesscodegroup/mvn-repo/raw/master/")
    maven(url = "https://mvn.lumine.io/repository/maven-public/") {
        content {
            includeModule("me.robin", "BattleLevels")
        }
    }
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/") {
        content {
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
    api(projects.mimicBukkitApi)

    compileOnly(libs.paperApi)
    compileOnly(libs.annotations)

    implementation(libs.bstats)
    implementation(libs.serialization.hocon)

    compileOnly(libs.commandapi)
    compileOnly(libs.commandapi.kotlin)
    compileOnly(libs.bundles.rpgplugins) { isTransitive = false }

    // From libs/ directory
    compileOnly(":CustomItemsAPI")
    compileOnly(":QuantumRPG:5.10.2")
    compileOnly(":NexEngine:2.0.3") // Do not update NexEngine. QuantumRpgWrapper cannot compile with a higher version

    testImplementation(libs.paperApi)
    testImplementation(libs.rpgplugins.skillapi)
}

kotlin {
    compilerOptions {
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}

tasks.test {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
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

    exclude("META-INF/*.kotlin_module")
    exclude("META-INF/com.android.tools/**")
    exclude("META-INF/proguard/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/**/module-info.class")
    exclude("LICENSE")

    minimize()
}
