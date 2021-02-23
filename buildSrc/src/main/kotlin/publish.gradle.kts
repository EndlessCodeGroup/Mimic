plugins {
    kotlin("jvm")
    `maven-publish`
}

java {
    withSourcesJar()
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    val isReleaseVersion = !version.toString().endsWith("-SNAPSHOT")
    repositories {
        if (isReleaseVersion) ossrh()
    }
}

fun RepositoryHandler.ossrh() {
    maven("https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
        name = "ossrh"
        credentials(PasswordCredentials::class)
    }
}
