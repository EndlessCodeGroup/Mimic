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
import ru.endlesscode.mimic.bukkit.command.LevelSystemSubcommand
import ru.endlesscode.mimic.bukkit.command.MainCommand
import ru.endlesscode.mimic.bukkit.impl.battlelevels.BattleLevelsLevelSystem
import ru.endlesscode.mimic.bukkit.impl.skillapi.SkillApiClassSystem
import ru.endlesscode.mimic.bukkit.impl.skillapi.SkillApiLevelSystem
import ru.endlesscode.mimic.bukkit.impl.vanilla.MinecraftLevelSystem
import ru.endlesscode.mimic.bukkit.impl.vanilla.PermissionsClassSystem
import ru.endlesscode.mimic.bukkit.internal.Log

/** Main class of the plugin. */
class Mimic : JavaPlugin() {

    companion object {
        private const val DEBUG = true
    }

    /** Default subsystems */
    private val defaultSubsystems = listOf(
        MinecraftLevelSystem.provider to ServicePriority.Lowest,
        PermissionsClassSystem.provider to ServicePriority.Lowest,
        SkillApiClassSystem.provider to ServicePriority.Normal,
        SkillApiLevelSystem.provider to ServicePriority.Normal,
        BattleLevelsLevelSystem.provider to ServicePriority.Normal
    )

    private inline val servicesManager get() = server.servicesManager

    override fun onLoad() {
        Log.init(logger, DEBUG)
        hookDefaultSystems()
    }

    override fun onEnable() {
        registerCommands()
    }

    private fun hookDefaultSystems() {
        defaultSubsystems.forEach { (service, priority) ->
            hookService(service, priority)
        }
    }

    private fun <T : MimicService> hookService(service: T, priority: ServicePriority) {
        val serviceClass = service.javaClass
        if (service.isEnabled) {
            servicesManager.register(serviceClass, service, this, priority)
            Log.d("Subsystem '${serviceClass.name}' registered.")
        } else {
            Log.d("Subsystem '${serviceClass.name}' not needed. Skipped.")
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

        servicesManager.getProvider<BukkitLevelSystem.Provider>()?.let {
            manager.registerCommand(LevelSystemSubcommand(it))
        }
        servicesManager.getProvider<BukkitClassSystem.Provider>()?.let {
            manager.registerCommand(ClassSystemSubcommand(it))
        }
    }

    override fun onDisable() {
        servicesManager.unregisterAll(this)
    }
}
