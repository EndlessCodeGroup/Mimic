plugins {
    id("com.vanniktech.maven.publish")
    signing
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    pom {
        val gitHubProject = "EndlessCodeGroup/Mimic"
        name = provider { project.name }
        description = provider { project.description }
        url = "https://github.com/$gitHubProject/"

        scm {
            url = "https://github.com/$gitHubProject/"
            connection = "scm:git:git://github.com/$gitHubProject.git"
            developerConnection = "scm:git:ssh://git@github.com:$gitHubProject.git"
        }

        licenses {
            license {
                name = "GNU Affero General Public License v3.0"
                url = "https://choosealicense.com/licenses/agpl-3.0/"
            }
        }

        developers {
            developer {
                id = "osipxd"
                name = "Osip Fatkullin"
                email = "osip.fatkullin@gmail.com"
            }
        }
    }
}

signing {
    if (hasProperty("signing.gnupg.keyName")) useGpgCmd()
}
