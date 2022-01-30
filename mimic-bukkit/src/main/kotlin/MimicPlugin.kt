/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
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

package ru.endlesscode.mimic

import co.aikar.commands.PaperCommandManager
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.ServicePriority.*
import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.mimic.bukkit.load
import ru.endlesscode.mimic.bukkit.loadAll
import ru.endlesscode.mimic.bukkit.register
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.command.ClassSystemSubcommand
import ru.endlesscode.mimic.command.ItemsSubcommand
import ru.endlesscode.mimic.command.LevelSystemSubcommand
import ru.endlesscode.mimic.command.MainCommand
import ru.endlesscode.mimic.impl.battlelevels.BattleLevelsLevelSystem
import ru.endlesscode.mimic.impl.customitems.CustomItemsRegistry
import ru.endlesscode.mimic.impl.heroes.HeroesClassSystem
import ru.endlesscode.mimic.impl.heroes.HeroesLevelSystem
import ru.endlesscode.mimic.impl.mimic.MimicItemsRegistry
import ru.endlesscode.mimic.impl.mimic.PermissionsClassSystem
import ru.endlesscode.mimic.impl.mmocore.MmoCoreClassSystem
import ru.endlesscode.mimic.impl.mmocore.MmoCoreLevelSystem
import ru.endlesscode.mimic.impl.mmoitems.MmoItemsRegistry
import ru.endlesscode.mimic.impl.quantumrpg.QuantumRpgClassSystem
import ru.endlesscode.mimic.impl.quantumrpg.QuantumRpgItemsRegistry
import ru.endlesscode.mimic.impl.quantumrpg.QuantumRpgLevelSystem
import ru.endlesscode.mimic.impl.skillapi.SkillApiClassSystem
import ru.endlesscode.mimic.impl.skillapi.SkillApiLevelSystem
import ru.endlesscode.mimic.impl.vanilla.MinecraftItemsRegistry
import ru.endlesscode.mimic.impl.vanilla.MinecraftLevelSystem
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.util.checkClassesLoaded

/** Main class of the plugin. */
public class MimicPlugin : JavaPlugin() {

    private val isReleased = !description.version.endsWith("-SNAPSHOT")

    private val mimic: Mimic by lazy { MimicImpl(servicesManager) }

    private inline val servicesManager get() = server.servicesManager

    override fun onLoad() {
        Log.init(logger, debug = !isReleased)
        servicesManager.register(mimic, this)
        hookDefaultServices()
    }

    override fun onEnable() {
        if (isReleased) initMetrics()
        registerCommands()
    }

    private fun hookDefaultServices() {
        // Default systems
        Log.i(">>> Default systems")
        hookLevels(MinecraftLevelSystem::Provider, priority = Lowest)
        hookClasses(PermissionsClassSystem::Provider, priority = Lowest)
        hookItems(::MinecraftItemsRegistry, priority = Lowest)
        hookItems({ MimicItemsRegistry(servicesManager) }, priority = Highest)

        pluginHooks("SkillAPI", "com.sucy.skill.SkillAPI") {
            hookLevels(SkillApiLevelSystem::Provider)
            hookClasses(SkillApiClassSystem::Provider)
        }

        pluginHooks("BattleLevels", "me.robin.battlelevels.api.BattleLevelsAPI") {
            hookLevels(BattleLevelsLevelSystem::Provider)
        }

        pluginHooks("MMOCore", "net.Indyuce.mmocore.MMOCore") {
            hookLevels(MmoCoreLevelSystem::Provider)
            hookClasses(MmoCoreClassSystem::Provider)
        }

        pluginHooks("Heroes", "com.herocraftonline.heroes.Heroes") {
            hookLevels(HeroesLevelSystem::Provider)
            hookClasses(HeroesClassSystem::Provider)
        }

        pluginHooks("QuantumRPG", "su.nightexpress.quantumrpg.QuantumRPG") {
            hookLevels(QuantumRpgLevelSystem::Provider)
            hookClasses(QuantumRpgClassSystem::Provider)
            hookItems(::QuantumRpgItemsRegistry)
        }

        pluginHooks("CustomItems", "com.jojodmo.customitems.api.CustomItemsAPI") {
            hookItems(::CustomItemsRegistry)
        }

        pluginHooks("MMOItems", "net.Indyuce.mmoitems.MMOItems") {
            hookItems(::MmoItemsRegistry)
        }
    }

    private fun pluginHooks(name: String, vararg requiredClasses: String, hooks: () -> Unit) {
        val pluginLoaded = Bukkit.getPluginManager().getPlugin(name) != null
        if (pluginLoaded && checkClassesLoaded(*requiredClasses)) {
            Log.i(">>> $name hooks")
            hooks()
        }
    }

    //<editor-fold defaultstate="collapsed" desc="hook* methods">
    private fun hookLevels(
        constructor: () -> BukkitLevelSystem.Provider,
        priority: ServicePriority = Normal,
    ) {
        try {
            val provider = mimic.registerLevelSystem(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
                ?: return
            reportServiceHooked(provider)
        } catch (e: Exception) {
            Log.d(e)
        }
    }

    private fun hookClasses(
        constructor: () -> BukkitClassSystem.Provider,
        priority: ServicePriority = Normal,
    ) {
        try {
            val provider = mimic.registerClassSystem(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
                ?: return
            reportServiceHooked(provider)
        } catch (e: Exception) {
            Log.d(e)
        }
    }

    private fun hookItems(
        constructor: () -> BukkitItemsRegistry,
        priority: ServicePriority = Normal,
    ) {
        try {
            val provider = mimic.registerItemsRegistry(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
                ?: return
            reportServiceHooked(provider)
        } catch (e: Exception) {
            Log.d(e)
        }
    }

    private inline fun <reified ServiceT : MimicService> reportServiceHooked(service: ServiceT) {
        val serviceName = ServiceT::class.java.name
            .substringAfterLast(".Bukkit")
            .substringBefore("$")
        logger.info("$serviceName '${service.id}' registered")
    }
    //</editor-fold>

    private fun initMetrics() {
        val metrics = Metrics(this, 8413)

        metrics.addCustomChart(Metrics.SimplePie("level_system") {
            loadService<BukkitLevelSystem.Provider>().id
        })
        metrics.addCustomChart(Metrics.SimplePie("class_system") {
            loadService<BukkitClassSystem.Provider>().id
        })
        metrics.addCustomChart(Metrics.AdvancedPie("items_registry_custom") {
            servicesManager.loadAll<BukkitItemsRegistry>()
                .map { it.id }
                .filterNot { it == MimicItemsRegistry.ID || it == MinecraftItemsRegistry.ID }
                .associateWith { 1 }
        })
    }

    @Suppress("DEPRECATION") // Yes. I want to use unstable API
    private fun registerCommands() {
        val manager = PaperCommandManager(this)
        manager.enableUnstableAPI("help")
        manager.enableUnstableAPI("brigadier")
        manager.commandReplacements.addReplacements(
            "command", "mimic",
            "perm", "mimic.admin"
        )

        manager.registerCommand(MainCommand(this))
        manager.registerCommand(LevelSystemSubcommand(mimic))
        manager.registerCommand(ClassSystemSubcommand(mimic))
        manager.registerCommand(ItemsSubcommand(loadService()))
    }

    private inline fun <reified T : Any> loadService(): T {
        return checkNotNull(servicesManager.load())
    }
}
