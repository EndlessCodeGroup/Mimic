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

package ru.endlesscode.mimic

import co.aikar.commands.PaperCommandManager
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.ServicePriority.*
import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.mimic.bukkit.load
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
import ru.endlesscode.mimic.impl.quantumrpg.QuantumRpgLevelSystem
import ru.endlesscode.mimic.impl.skillapi.SkillApiClassSystem
import ru.endlesscode.mimic.impl.skillapi.SkillApiLevelSystem
import ru.endlesscode.mimic.impl.vanilla.MinecraftItemsRegistry
import ru.endlesscode.mimic.impl.vanilla.MinecraftLevelSystem
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.util.checkClassesLoaded
import kotlin.reflect.KClass

/** Main class of the plugin. */
public class Mimic : JavaPlugin() {

    private val isReleased = !description.version.endsWith("-SNAPSHOT")

    private inline val servicesManager get() = server.servicesManager

    override fun onLoad() {
        Log.init(logger, debug = !isReleased)
        hookDefaultServices()
    }

    override fun onEnable() {
        if (isReleased) initMetrics()
        registerCommands()
    }

    private fun hookDefaultServices() {
        // LevelSystem
        hookLevels(MinecraftLevelSystem::Provider, Lowest)
        hookLevels(SkillApiLevelSystem::Provider, Normal, "com.sucy.skill.SkillAPI")
        hookLevels(BattleLevelsLevelSystem::Provider, Normal, "me.robin.battlelevels.api.BattleLevelsAPI")
        hookLevels(MmoCoreLevelSystem::Provider, Normal, "net.Indyuce.mmocore.MMOCore")
        hookLevels(HeroesLevelSystem::Provider, Normal, "com.herocraftonline.heroes.Heroes")
        hookLevels(QuantumRpgLevelSystem::Provider, Normal, "su.nightexpress.quantumrpg.QuantumRPG")

        // ClassSystem
        hookClasses(PermissionsClassSystem::Provider, Lowest)
        hookClasses(SkillApiClassSystem::Provider, Normal, "com.sucy.skill.SkillAPI")
        hookClasses(MmoCoreClassSystem::Provider, Normal, "net.Indyuce.mmocore.MMOCore")
        hookClasses(HeroesClassSystem::Provider, Normal, "com.herocraftonline.heroes.Heroes")
        hookClasses(QuantumRpgClassSystem::Provider, Normal, "su.nightexpress.quantumrpg.QuantumRPG")

        // ItemsRegistry
        hookItems(::MinecraftItemsRegistry, Lowest)
        hookItems(::CustomItemsRegistry, Normal, "com.jojodmo.customitems.api.CustomItemsAPI")
        hookItems(::MmoItemsRegistry, Normal, "net.Indyuce.mmoitems.MMOItems")
        hookItems({ MimicItemsRegistry(servicesManager) }, Highest)
    }

    //<editor-fold defaultstate="collapsed" desc="hook* methods">
    private fun hookLevels(
        constructor: () -> BukkitLevelSystem.Provider,
        priority: ServicePriority,
        vararg requiredPackages: String
    ) {
        hookService(BukkitLevelSystem.Provider::class, constructor, priority, requiredPackages)
    }

    private fun hookClasses(
        constructor: () -> BukkitClassSystem.Provider,
        priority: ServicePriority,
        vararg requiredPackages: String
    ) {
        hookService(BukkitClassSystem.Provider::class, constructor, priority, requiredPackages)
    }

    private fun hookItems(
        constructor: () -> BukkitItemsRegistry,
        priority: ServicePriority,
        vararg requiredPackages: String
    ) {
        hookService(BukkitItemsRegistry::class, constructor, priority, requiredPackages)
    }

    private fun <ServiceT : MimicService> hookService(
        serviceClass: KClass<ServiceT>,
        constructor: () -> ServiceT,
        priority: ServicePriority,
        requiredPackages: Array<out String>
    ) {
        try {
            if (checkClassesLoaded(*requiredPackages)) {
                val service = constructor()
                servicesManager.register(serviceClass.java, service, this, priority)
                val serviceName = serviceClass.java.name
                    .replace(Regex(".*\\.Bukkit"), "")
                    .substringBefore("$")
                logger.info("[$serviceName] '${service.id}' found")
            }
        } catch (e: Exception) {
            Log.d(e)
        }
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
        manager.registerCommand(LevelSystemSubcommand(loadService()))
        manager.registerCommand(ClassSystemSubcommand(loadService()))
        manager.registerCommand(ItemsSubcommand(loadService()))
    }

    private inline fun <reified T : Any> loadService(): T {
        return checkNotNull(servicesManager.load())
    }
}
