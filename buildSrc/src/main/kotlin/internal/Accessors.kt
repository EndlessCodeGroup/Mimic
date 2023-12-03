package internal

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByName

internal val Project.versionCatalogs: VersionCatalogsExtension
    get() = extensions.getByName<VersionCatalogsExtension>("versionCatalogs")
