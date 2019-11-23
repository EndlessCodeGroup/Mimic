
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(kotlin("gradle-plugin", version = "1.3.50"))
    implementation("gradle.plugin.ru.endlesscode:bukkit-gradle:0.8.2")
}

repositories {
    maven(url = "https://plugins.gradle.org/m2/")
}
