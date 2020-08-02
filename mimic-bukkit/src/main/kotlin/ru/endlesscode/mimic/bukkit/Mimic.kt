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

import co.aikar.commands.BukkitCommandManager
import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.ClassSystem
import ru.endlesscode.MimicSystem
import ru.endlesscode.mimic.api.system.registry.getSystemFactory
import ru.endlesscode.mimic.bukkit.command.ClassSystemSubcommand
import ru.endlesscode.mimic.bukkit.command.LevelSystemSubcommand
import ru.endlesscode.mimic.bukkit.command.MainCommand
import ru.endlesscode.mimic.bukkit.system.BukkitLevelSystem
import ru.endlesscode.mimic.bukkit.system.battlelevels.BattleLevelsLevelSystem
import ru.endlesscode.mimic.bukkit.system.skillapi.SkillApiClassSystem
import ru.endlesscode.mimic.bukkit.system.skillapi.SkillApiLevelSystem
import ru.endlesscode.mimic.bukkit.system.vanilla.PermissionsClassSystem
import ru.endlesscode.mimic.bukkit.system.vanilla.VanillaLevelSystem
import ru.endlesscode.mimic.bukkit.util.Log

/** Main class of the plugin. */
class Mimic : JavaPlugin() {

    companion object {
        private const val DEBUG = true
        private lateinit var instance: Mimic

        /** Get system registry that can be used to obtain player systems. */
        @JvmStatic
        val systemRegistry: BukkitSystemRegistry
            get() = instance.systemRegistry
    }

    /** Default subsystems */
    private val defaultSubsystems = listOf(
        VanillaLevelSystem::class.java,
        PermissionsClassSystem::class.java,
        SkillApiClassSystem::class.java,
        SkillApiLevelSystem::class.java,
        BattleLevelsLevelSystem::class.java
    )

    @get:JvmName("_getSystemRegistry") // Conflicts with static getSystemRegistry
    private val systemRegistry by lazy { BukkitSystemRegistry(this, server.servicesManager) }

    override fun onLoad() {
        instance = this
        Log.init(logger, DEBUG)
        hookDefaultSystems()
    }

    override fun onEnable() {
        registerCommands()
    }

    private fun hookDefaultSystems() {
        defaultSubsystems.forEach(::hookSystem)
    }

    private fun <T : MimicSystem> hookSystem(system: Class<out T>) {
        try {
            if (systemRegistry.registerSubsystem(system)) {
                Log.d("Subsystem '${system.simpleName}' registered.")
            } else {
                Log.d("Subsystem '${system.simpleName}' not needed. Skipped.")
            }
        } catch (e: registry.SystemNotRegisteredException) {
            Log.w("[${system.simpleName}] ${e.message}")
            Log.d(e)
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

        systemRegistry.getSystemFactory<BukkitLevelSystem>()?.let {
            manager.registerCommand(LevelSystemSubcommand(it))
        }
        systemRegistry.getSystemFactory<ClassSystem>()?.let {
            manager.registerCommand(ClassSystemSubcommand(it))
        }
    }

    override fun onDisable() {
        systemRegistry.unregisterAllSubsystems()
    }
}
