/*
 * This file is part of RPGInventory.
 * Copyright (C) 2019 EndlessCode Group and contributors
 *
 * RPGInventory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RPGInventory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with RPGInventory.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

private const val JAVA_8 = "1.8"

/** Default project configurations. */
fun Project.configureProject() {
    apply(plugin = "kotlin")

    tasks.withType(JavaCompile::class) {
        sourceCompatibility = JAVA_8
        targetCompatibility = JAVA_8
        options.encoding = "UTF-8"
    }

    tasks.withType(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "1.8"
            apiVersion = "1.3"
            languageVersion = "1.3"
            freeCompilerArgs = listOf("-Xjvm-default=enable")
            javaParameters = true
        }
    }

    dependencies {
        "implementation"(kotlin("stdlib-jdk8"))
        testingDependencies()
    }

    repositories {
        jcenter()
    }

    configurePublish()
}

fun DependencyHandlerScope.testingDependencies() {
    "testImplementation"(kotlin("test-junit"))
    "testImplementation"(deps.junit)
    "testImplementation"(deps.mockitoInline)
    "testImplementation"(deps.mockito)
}

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
