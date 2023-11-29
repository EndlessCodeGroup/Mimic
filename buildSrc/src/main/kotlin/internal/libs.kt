package internal

import gradle.kotlin.dsl.accessors._1f737d11fad22b9b058419dfc437a798.versionCatalogs
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
        get() = get("junit-bom")

    context(Project)
    val junit_jupiter
        get() = get("junit-jupiter")

    context(Project)
    val junit_jupiter_params
        get() = get("junit-jupiter-params")

    context(Project)
    val kotest_assertions
        get() = get("kotest-assertions")

    context(Project)
    val mockk
        get() = get("mockk")

    private fun Project.get(alias: String): Provider<MinimalExternalModuleDependency> =
        versionCatalogs.named("libs").findLibrary(alias).get()
}
