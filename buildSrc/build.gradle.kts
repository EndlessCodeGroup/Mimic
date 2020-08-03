
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(kotlin("gradle-plugin", version = "1.3.72"))
    implementation("gradle.plugin.ru.endlesscode:bukkit-gradle:0.8.2")
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
}

repositories {
    jcenter()
    gradlePluginPortal()
}
