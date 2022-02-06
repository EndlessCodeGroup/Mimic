plugins {
    `java-base`
    `maven-publish`
    signing
}

java {
    withSourcesJar()
    withJavadocJar()
}

val isReleaseVersion = !version.toString().endsWith("-SNAPSHOT")
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                val gitHubProject = "EndlessCodeGroup/Mimic"
                name.set(provider { project.name })
                description.set(provider { project.description })
                url.set("https://github.com/$gitHubProject/")

                scm {
                    url.set("https://github.com/$gitHubProject/")
                    connection.set("scm:git:git://github.com/$gitHubProject.git")
                    developerConnection.set("scm:git:ssh://git@github.com:$gitHubProject.git")
                }

                licenses {
                    license {
                        name.set("GNU Affero General Public License v3.0")
                        url.set("https://choosealicense.com/licenses/agpl-3.0/")
                    }
                }

                developers {
                    developer {
                        id.set("osipxd")
                        name.set("Osip Fatkullin")
                        email.set("osip.fatkullin@gmail.com")
                    }
                }
            }
        }
    }

    repositories {
        if (isReleaseVersion) {
            maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
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
