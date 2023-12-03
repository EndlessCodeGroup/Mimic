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

    context(Project)
    val junit_bom
        get() = lib("junit-bom")

    context(Project)
    val junit_jupiter
        get() = lib("junit-jupiter")

    context(Project)
    val junit_jupiter_params
        get() = lib("junit-jupiter-params")

    context(Project)
    val kotest_assertions
        get() = lib("kotest-assertions")

    context(Project)
    val mockk
        get() = lib("mockk")

    private fun Project.lib(alias: String): Provider<MinimalExternalModuleDependency> =
        versionCatalogs.named("libs").findLibrary(alias).get()
}
