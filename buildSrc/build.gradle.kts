plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(16)
}

dependencies {
    val kotlinVersion = "1.9.20"
    implementation(kotlin("gradle-plugin", version = kotlinVersion))
    implementation(kotlin("serialization", version = kotlinVersion))
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.9.10")
    implementation("org.jetbrains.kotlinx:binary-compatibility-validator:0.13.2")
    implementation("de.undercouch:gradle-download-task:5.5.0")
}

repositories {
    mavenCentral()
}
