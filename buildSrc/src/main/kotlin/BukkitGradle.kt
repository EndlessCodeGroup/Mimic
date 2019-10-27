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

import groovy.lang.Closure
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.delegateClosureOf
import ru.endlesscode.bukkitgradle.extension.Bukkit
import ru.endlesscode.bukkitgradle.extension.RunConfiguration
import ru.endlesscode.bukkitgradle.meta.PluginMeta

// Utils to make BukkitGradle kotlin-friendly

val DependencyHandlerScope.bukkit: Any
    get() = (extensions.extraProperties["bukkit"] as Closure<*>).call()

fun Bukkit.meta(configure: PluginMeta.() -> Unit) {
    meta(delegateClosureOf(configure))
}

fun Bukkit.run(configure: RunConfiguration.() -> Unit) {
    run(delegateClosureOf(configure))
}
