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
        externalDocumentationLinks.register("paper") {
            url("https://jd.papermc.io/paper/1.20.6/")
            packageListUrl("https://jd.papermc.io/paper/1.20.6/element-list")
        }
        externalDocumentationLinks.register("adventure-api") {
            url("https://jd.advntr.dev/api/4.17.0/")
            packageListUrl("https://jd.advntr.dev/api/4.17.0/element-list")
        }
        externalDocumentationLinks.register("adventure-key") {
            url("https://jd.advntr.dev/key/4.17.0/")
            packageListUrl("https://jd.advntr.dev/key/4.17.0/element-list")
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
