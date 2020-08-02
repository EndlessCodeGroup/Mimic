/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2018 Osip Fatkullin
 * Copyright (C) 2018 EndlessCode Group and contributors
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
import org.bukkit.plugin.ServicesManager
import ru.endlesscode.MimicSystem
import ru.endlesscode.SystemFactory

/** Implementation of system registry for bukkit. Using [ServicesManager] */
class BukkitSystemRegistry internal constructor(
    private val plugin: Plugin,
    private val servicesManager: ServicesManager
) : registry.SystemRegistry() {

    override fun <FactoryT : SystemFactory<*>> registerFactory(
        factoryClass: Class<FactoryT>,
        subsystemFactory: FactoryT,
        priority: registry.SubsystemPriority
    ) {
        val servicePriority = priority.toServicePriority()
        servicesManager.register(factoryClass, subsystemFactory, plugin, servicePriority)
    }

    override fun <SystemT : MimicSystem> getFactory(
        factoryClass: Class<out SystemFactory<SystemT>>
    ): SystemFactory<SystemT>? {
        return servicesManager.load(factoryClass)
    }

    override fun unregisterAllSubsystems() {
        servicesManager.unregisterAll(plugin)
    }

    override fun <SystemT : MimicSystem> unregisterFactory(factory: SystemFactory<out SystemT>) {
        servicesManager.unregister(factory)
    }
}
