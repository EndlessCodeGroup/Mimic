import internal.libs
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
    explicitApi()

    compilerOptions {
        jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
        allWarningsAsErrors = providers.gradleProperty("warningsAsErrors").map { it.toBoolean() }.orElse(false)
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    testingDependencies()
}

repositories {
    mavenCentral()
}

dokka {
    dokkaSourceSets.configureEach {
        reportUndocumented = true
        sourceLink {
            localDirectory = file("src/main/kotlin/")
            remoteUrl("https://github.com/EndlessCodeGroup/Mimic/tree/develop/${project.name}/src/main/kotlin/")
        }
        externalDocumentationLinks.register("spigot") {
            url("https://hub.spigotmc.org/javadocs/spigot/")
            packageListUrl("https://gist.githubusercontent.com/osipxd/604c9b3f91c3a6c56050f4a3b027f333/raw/package-list")
        }
    }
    pluginsConfiguration.html {
        separateInheritedMembers = true
    }
}

fun DependencyHandlerScope.testingDependencies() {
    testImplementation(kotlin("test-junit5"))
    testImplementation(platform(libs.junit_bom))
    testImplementation(libs.junit_jupiter)
    testImplementation(libs.junit_jupiter_params)
    testImplementation(libs.kotest_assertions)
    testImplementation(libs.mockk)
}
