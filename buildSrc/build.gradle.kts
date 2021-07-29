plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(kotlin("gradle-plugin", version = "1.5.21"))
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.5.0")
    implementation("org.jetbrains.kotlinx:binary-compatibility-validator:0.6.0")
    implementation("de.undercouch:gradle-download-task:4.1.2")
}

repositories {
    mavenCentral()
}
