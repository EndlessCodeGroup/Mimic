/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2020 Osip Fatkullin
 * Copyright (C) 2020 EndlessCode Group and contributors
 *
 * BukkitMimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitMimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BukkitMimic.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.bukkit

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.ServicesManager

/** Kotlin-style call of [ServicesManager.register]. */
public inline fun <reified T : Any> ServicesManager.register(
    provider: T,
    plugin: Plugin,
    priority: ServicePriority = ServicePriority.Normal,
) {
    register(T::class.java, provider, plugin, priority)
}

/** Kotlin-style call of [ServicesManager.load]. */
public inline fun <reified T : Any> ServicesManager.load(): T? {
    return load(T::class.java)
}

/** Kotlin-style call of [ServicesManager.getRegistrations] followed with [RegisteredServiceProvider.getProvider]. */
public inline fun <reified T : Any> ServicesManager.loadAll(): Collection<T> {
    return getRegistrations(T::class.java).map { it.provider }
}

/** Kotlin-style call of [ServicesManager.getRegistrations]. */
public inline fun <reified T : Any> ServicesManager.getRegistrations(): Collection<RegisteredServiceProvider<T>> {
    return getRegistrations(T::class.java)
}
