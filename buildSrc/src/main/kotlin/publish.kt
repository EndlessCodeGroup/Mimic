import com.jfrog.bintray.gradle.BintrayExtension
import internal.java
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import java.util.*

fun Project.configurePublish() {
    apply(plugin = "maven-publish")

    java {
        @Suppress("UnstableApiUsage")
        withSourcesJar()
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
}

fun Project.configureBintray(
    repoUrl: String,
    projectName: String = this.name
) {
    val bintrayUsername = System.getProperty("bintray.username")
    val bintrayApiKey = System.getProperty("bintray.apiKey")
    if (bintrayUsername == null || bintrayApiKey == null) {
        println(
            """
            System properties 'bintray.username' and 'bintray.apiKey' are not specified.
            Publication to Bintray is unavailable.
            https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_system_properties
        """.trimIndent())
        return
    }

    apply(plugin = "com.jfrog.bintray")

    bintray {
        user = bintrayUsername
        key = bintrayApiKey
        setPublications("maven")

        pkg {
            repo = "repo"
            name = projectName.toLowerCase()
            userOrg = "endlesscode"
            setLicenses("AGPL-v3")
            setLabels("plugin", "bukkit", "api", "minecraft", "rpg")
            publicDownloadNumbers = true
            vcsUrl = "$repoUrl.git"
            websiteUrl = repoUrl
            issueTrackerUrl = "$repoUrl/issues"

            version {
                name = project.version.toString()
                desc = "$projectName v$name"
                released = Date().toString()
                vcsTag = "v$name"
            }
        }
    }
}

private fun Project.bintray(configure: BintrayExtension.() -> Unit) {
    extensions.configure("bintray", configure)
}

private fun BintrayExtension.pkg(configure: BintrayExtension.PackageConfig.() -> Unit) {
    pkg.apply(configure)
}

private fun BintrayExtension.PackageConfig.version(configure: BintrayExtension.VersionConfig.() -> Unit) {
    version.apply(configure)
}
