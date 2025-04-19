plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

dependencies {
    val kotlinVersion = libs.versions.kotlin.get()
    implementation(kotlin("gradle-plugin", version = kotlinVersion))
    implementation(kotlin("serialization", version = kotlinVersion))
    implementation(libs.dokka)
    implementation(libs.kotlinx.binaryCompatibilityValidator)
}

repositories {
    mavenCentral()
}
