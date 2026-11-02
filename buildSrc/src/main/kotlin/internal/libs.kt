package internal

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

/**
 * Accessors for version catalogs.
 * It is not generated automatically for precompiled build scripts.
 */
@Suppress("ClassName")
internal object libs {

    context(project: Project)
    val junit_bom
        get() = project.lib("junit-bom")

    context(project: Project)
    val junit_jupiter
        get() = project.lib("junit-jupiter")

    context(project: Project)
    val junit_jupiter_params
        get() = project.lib("junit-jupiter-params")

    context(project: Project)
    val kotest_assertions
        get() = project.lib("kotest-assertions")

    context(project: Project)
    val mockk
        get() = project.lib("mockk")

    private fun Project.lib(alias: String): Provider<MinimalExternalModuleDependency> =
        versionCatalogs.named("libs").findLibrary(alias).get()
}
