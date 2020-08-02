import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension

internal fun Project.java(configure: JavaPluginExtension.() -> Unit) {
    extensions.configure("java", configure)
}
