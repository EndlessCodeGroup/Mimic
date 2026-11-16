import ru.endlesscode.bukkitgradle.dependencies.aikar
import ru.endlesscode.bukkitgradle.dependencies.codemc
import ru.endlesscode.bukkitgradle.dependencies.papermc

plugins {
    commons
    publish
    alias(libs.plugins.shadow)
    alias(libs.plugins.bukkitgradle)
    alias(libs.plugins.minotaur)
    alias(libs.plugins.hangar)
    kotlin("plugin.serialization")
}

description = "Bukkit plugin implementing Mimic APIs"

bukkit {
    apiVersion = "1.20.6"

    plugin {
        name = "Mimic"
        main = "ru.endlesscode.mimic.MimicPlugin"
        authors = listOf("osipxd", "EndlessCodeGroup")
        website = "https://github.com/EndlessCodeGroup/Mimic"
        loadBefore = listOf(
            "CustomItems",
            "MMOCore",
            "MMOItems",
            "Heroes",
            "CraftEngine",
            "AuraSkills",
            "mcMMO",
            "Jobs",
            "Fabled",
            "ValhallaMMO",
            "ItemsAdder",
        )
    }

    server {
        version = "1.21.5"
        eula = true
    }
}

repositories {
    papermc()
    maven(url = "https://gitlab.com/endlesscodegroup/mvn-repo/raw/master/")
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/") {
        content {
            includeGroup("net.Indyuce")
            includeModule("io.lumine", "MythicLib-dist")
        }
    }
    maven(url = "https://repo.momirealms.net/releases/") {
        content {
            includeGroup("net.momirealms")
        }
    }
    maven(url = "https://nexus.neetgames.com/repository/maven-releases/") {
        content {
            includeGroup("com.gmail.nossr50.mcMMO")
        }
    }
    maven(url = "https://jitpack.io") {
        content {
            includeGroup("com.github.Zrips")
        }
    }
    maven(url = "https://repo.travja.dev/releases/") {
        content {
            includeGroup("studio.magemonkey")
        }
    }
    maven(url = "https://repo.repsy.io/mvn/athlaeos/valhallammo/") {
        content {
            includeGroup("me.athlaeos")
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

    compileOnly(libs.bundles.rpgplugins) { isTransitive = false }

    // From libs/ directory
    compileOnly(":CustomItemsAPI")

    testImplementation(libs.paperApi)
    testImplementation(libs.rpgplugins.auraskills)
    testImplementation(libs.rpgplugins.mcmmo) { isTransitive = false }
    testImplementation(libs.rpgplugins.jobs) { isTransitive = false }
    testImplementation(libs.rpgplugins.cmilib) { isTransitive = false }
    testImplementation(libs.rpgplugins.valhallammo) { isTransitive = false }
    testImplementation(libs.rpgplugins.heroes) { isTransitive = false }
    testImplementation(libs.rpgplugins.mmocore) { isTransitive = false }
    testImplementation(libs.rpgplugins.mmoitems) { isTransitive = false }
    testImplementation(libs.rpgplugins.mythiclib) { isTransitive = false }
    testImplementation(libs.rpgplugins.fabled) { isTransitive = false }
    testImplementation(libs.rpgplugins.codex) { isTransitive = false }
    testImplementation(libs.rpgplugins.itemsadder)
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}

dokka {
    dokkaSourceSets.configureEach {
        reportUndocumented = false
    }
}

tasks.test {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.runServer {
    downloadPlugins {
        modrinth("auraskills", libs.versions.auraskills.get())
        modrinth("valhallammo", libs.versions.valhallammo.free.get())
        val mcmmoVersion = libs.versions.mcmmo.get()
        url("https://nexus.neetgames.com/repository/maven-releases/com/gmail/nossr50/mcMMO/mcMMO/$mcmmoVersion/mcMMO-$mcmmoVersion.jar")
        val jobsVersion = libs.versions.jobs.get()
        url("https://jitpack.io/com/github/Zrips/Jobs/$jobsVersion/Jobs-$jobsVersion.jar")
        val cmilibVersion = libs.versions.cmilib.get()
        url("https://jitpack.io/com/github/Zrips/CMILib/$cmilibVersion/CMILib-$cmilibVersion.jar")
        val fabledVersion = libs.versions.fabled.get()
        url("https://repo.travja.dev/releases/studio/magemonkey/fabled/$fabledVersion/fabled-$fabledVersion.jar")
        val codexVersion = libs.versions.codex.get()
        url("https://repo.travja.dev/releases/studio/magemonkey/codex/$codexVersion/codex-$codexVersion.jar")
    }
}

val supportedGameVersions = listOf(
    "1.20.6",
    "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6",
    "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11",
    "26.1", "26.1.1", "26.1.2",
)

modrinth {
    token = providers.environmentVariable("MODRINTH_TOKEN")
    projectId = "mimic-api"
    versionName = "Mimic ${project.version}"
    versionType = "release"
    uploadFile.set(tasks.shadowJar)
    changelog = providers.environmentVariable("RELEASE_NOTES").orElse("")
    loaders.addAll("paper", "purpur")
    gameVersions.addAll(supportedGameVersions)
    dependencies {
        optional.project("auraskills")
        optional.project("craftengine")
        optional.project("valhallammo")
    }
}

hangarPublish {
    publications.register("plugin") {
        apiKey = providers.environmentVariable("HANGAR_API_TOKEN")
        id = "Mimic"
        version = project.version.toString()
        channel = "Release"
        changelog = providers.environmentVariable("RELEASE_NOTES").orElse("")
        platforms {
            paper {
                jar = tasks.shadowJar.flatMap { it.archiveFile }
                platformVersions = supportedGameVersions
                dependencies {
                    hangar("AuraSkills") { required = false }
                    hangar("Fabled") { required = false }
                    hangar("ValhallaMMO") { required = false }
                    url("CraftEngine", "https://modrinth.com/plugin/craftengine") { required = false }
                }
            }
        }
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
