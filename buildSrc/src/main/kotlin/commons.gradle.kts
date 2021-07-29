import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
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
        apiVersion = "1.4"
        languageVersion = "1.4"
        freeCompilerArgs = listOf("-Xjvm-default=enable")
        allWarningsAsErrors = System.getProperty("warningsAsErrors") == "true"
        javaParameters = true
    }
}

extensions.configure<KotlinProjectExtension> {
    explicitApi()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testingDependencies()
}

repositories {
    mavenCentral()
}

fun DependencyHandlerScope.testingDependencies() {
    testImplementation(kotlin("test-junit5"))
    testImplementation(junit.jupiter)
    testImplementation(junit.jupiter_params)
    testImplementation(mockito.kotlin)
    testImplementation(mockito.inline)
}
