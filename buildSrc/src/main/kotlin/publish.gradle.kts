plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}

java {
    withSourcesJar()
}

val isReleaseVersion = !version.toString().endsWith("-SNAPSHOT")
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        if (isReleaseVersion) {
            maven("https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
                name = "ossrh"
                credentials(PasswordCredentials::class)
            }
        }
    }
}

signing {
    if (hasProperty("signing.gnupg.keyName")) useGpgCmd()
    sign(publishing.publications["maven"])
}
