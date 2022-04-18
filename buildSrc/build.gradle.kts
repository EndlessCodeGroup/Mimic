plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    val kotlinVersion = "1.6.20"
    implementation(kotlin("gradle-plugin", version = kotlinVersion))
    implementation(kotlin("serialization", version = kotlinVersion))
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.6.20")
    implementation("org.jetbrains.kotlinx:binary-compatibility-validator:0.9.0")
    implementation("de.undercouch:gradle-download-task:5.0.5")
}

repositories {
    mavenCentral()
}
