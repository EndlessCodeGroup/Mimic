import internal.libs
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("com.diffplug.spotless")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) { into("META-INF") }
}

spotless {
    kotlin {
        licenseHeaderFile(rootProject.file("spotless.license.kt"))
    }
}

kotlin {
    jvmToolchain(17)
    explicitApi()
    coreLibrariesVersion = "2.2.21"

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()

    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_2
        languageVersion = KotlinVersion.KOTLIN_2_2
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
