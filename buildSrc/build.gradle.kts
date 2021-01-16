
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(kotlin("gradle-plugin", version = "1.4.21"))
    implementation("de.undercouch:gradle-download-task:4.1.1")
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
}

repositories {
    jcenter()
}
