plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(kotlin("gradle-plugin", version = "1.4.30"))
    implementation("de.undercouch:gradle-download-task:4.1.1")
}

repositories {
    mavenCentral()
}
