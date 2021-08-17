import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        apiVersion = "1.5"
        languageVersion = "1.5"
        freeCompilerArgs = listOf("-Xjvm-default=all")
        allWarningsAsErrors = System.getProperty("warningsAsErrors") == "true"
        javaParameters = true
    }
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testingDependencies()
}

repositories {
    mavenCentral()
}

tasks.withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory.set(file("src/main/kotlin/"))
            remoteUrl.set(URL("https://github.com/EndlessCodeGroup/Mimic/tree/develop/${project.name}/src/main/kotlin/"))
        }
        externalDocumentationLink {
            url.set(URL("https://hub.spigotmc.org/javadocs/spigot/"))
            packageListUrl.set(URL("https://gist.githubusercontent.com/osipxd/604c9b3f91c3a6c56050f4a3b027f333/raw/package-list"))
        }
    }
}

fun DependencyHandlerScope.testingDependencies() {
    testImplementation(kotlin("test-junit5"))
    testImplementation(junit.jupiter)
    testImplementation(junit.jupiter_params)
    testImplementation(mockito.kotlin)
    testImplementation(mockito.inline)
}
