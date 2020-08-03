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

import co.aikar.commands.BukkitCommandManager
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.mimic.MimicService
import ru.endlesscode.mimic.bukkit.command.ClassSystemSubcommand
import ru.endlesscode.mimic.bukkit.command.ItemsServiceSubcommand
import ru.endlesscode.mimic.bukkit.command.LevelSystemSubcommand
import ru.endlesscode.mimic.bukkit.command.MainCommand
import ru.endlesscode.mimic.bukkit.impl.battlelevels.BattleLevelsLevelSystem
import ru.endlesscode.mimic.bukkit.impl.skillapi.SkillApiClassSystem
import ru.endlesscode.mimic.bukkit.impl.skillapi.SkillApiLevelSystem
import ru.endlesscode.mimic.bukkit.impl.vanilla.MinecraftItemsService
import ru.endlesscode.mimic.bukkit.impl.vanilla.MinecraftLevelSystem
import ru.endlesscode.mimic.bukkit.impl.vanilla.PermissionsClassSystem
import ru.endlesscode.mimic.bukkit.internal.Log
import kotlin.reflect.KClass

/** Main class of the plugin. */
class Mimic : JavaPlugin() {

    companion object {
        private const val DEBUG = true
    }

    private inline val servicesManager get() = server.servicesManager

    override fun onLoad() {
        Log.init(logger, DEBUG)
        hookDefaultServices()
    }

    override fun onEnable() {
        registerCommands()
    }

    @Suppress("RemoveExplicitTypeArguments") // We should specify type explicitly
    private fun hookDefaultServices() {
        Log.d("BukkitLevelSystem.Provider:")
        hookService<BukkitLevelSystem.Provider>(MinecraftLevelSystem.provider, ServicePriority.Lowest)
        hookService<BukkitLevelSystem.Provider>(SkillApiLevelSystem.provider, ServicePriority.Normal)
        hookService<BukkitLevelSystem.Provider>(BattleLevelsLevelSystem.provider, ServicePriority.Normal)
        Log.d("BukkitClassSystem.Provider:")
        hookService<BukkitClassSystem.Provider>(PermissionsClassSystem.provider, ServicePriority.Lowest)
        hookService<BukkitClassSystem.Provider>(SkillApiClassSystem.provider, ServicePriority.Normal)
        Log.d("BukkitItemsService:")
        hookService<BukkitItemsService>(MinecraftItemsService(), ServicePriority.Lowest)
    }

    private inline fun <reified ServiceT : MimicService> hookService(service: ServiceT, priority: ServicePriority) {
        hookService(ServiceT::class, service, priority)
    }

    private fun <T : MimicService> hookService(serviceClass: KClass<T>, service: T, priority: ServicePriority) {
        if (service.isEnabled) {
            servicesManager.register(serviceClass.java, service, this, priority)
            Log.d("- '${service.id}' registered.")
        } else {
            Log.d("- '${service.id}' not needed. Skipped.")
        }
    }

    private fun registerCommands() {
        val manager = BukkitCommandManager(this)
        @Suppress("DEPRECATION") // Yes. I want to use unstable API
        manager.enableUnstableAPI("help")
        manager.commandReplacements.addReplacements(
            "command", "mimic|m",
            "perm", "mimic.admin"
        )

        manager.registerCommand(MainCommand())

        servicesManager.load<BukkitLevelSystem.Provider>()?.let {
            manager.registerCommand(LevelSystemSubcommand(it))
        }
        servicesManager.load<BukkitClassSystem.Provider>()?.let {
            manager.registerCommand(ClassSystemSubcommand(it))
        }
        servicesManager.load<BukkitItemsService>()?.let {
            manager.registerCommand(ItemsServiceSubcommand(it))
        }
    }

    override fun onDisable() {
        servicesManager.unregisterAll(this)
    }
}
