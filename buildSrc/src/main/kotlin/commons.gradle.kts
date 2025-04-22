import internal.libs
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.net.URL

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
        apiVersion = KotlinVersion.KOTLIN_1_9
        languageVersion = KotlinVersion.KOTLIN_1_9
        freeCompilerArgs.add("-Xjvm-default=all")
        allWarningsAsErrors = System.getProperty("warningsAsErrors") == "true"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    testingDependencies()
}

repositories {
    mavenCentral()
}

tasks.withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
        reportUndocumented = true
        sourceLink {
            localDirectory = file("src/main/kotlin/")
            remoteUrl = URL("https://github.com/EndlessCodeGroup/Mimic/tree/develop/${project.name}/src/main/kotlin/")
        }
        externalDocumentationLink {
            url = URL("https://hub.spigotmc.org/javadocs/spigot/")
            packageListUrl = URL("https://gist.githubusercontent.com/osipxd/604c9b3f91c3a6c56050f4a3b027f333/raw/package-list")
        }
        pluginsMapConfiguration.put("org.jetbrains.dokka.base.DokkaBase", """{ "separateInheritedMembers": true}""")
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
