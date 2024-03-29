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

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bstats.bukkit.Metrics
import org.bstats.charts.AdvancedPie
import org.bstats.charts.SimplePie
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.ServicePriority.*
import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.mimic.bukkit.loadAll
import ru.endlesscode.mimic.bukkit.register
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.command.registerCommand
import ru.endlesscode.mimic.config.MimicConfig
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
import ru.endlesscode.mimic.impl.vanilla.MinecraftPlayerInventory
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.inventory.BukkitPlayerInventory
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.util.checkClassesLoaded

/** Main class of the plugin. */
public class MimicPlugin : JavaPlugin() {

    private val isReleased = !description.version.endsWith("-SNAPSHOT")

    private val config: MimicConfig by lazy { MimicConfig(this) }
    private val mimic: Mimic by lazy { MimicImpl(servicesManager, config) }
    private var audiences: BukkitAudiences? = null

    private inline val servicesManager get() = server.servicesManager
    private inline val pluginManager get() = server.pluginManager

    override fun onLoad() {
        Log.init(logger, debug = !isReleased)
        CommandAPI.onLoad(CommandAPIBukkitConfig(this))

        servicesManager.register(mimic, this)
        hookDefaultServices()
    }

    override fun onEnable() {
        CommandAPI.onEnable()
        audiences = BukkitAudiences.create(this)

        if (isReleased) initMetrics()
        registerCommand(
            mimic = mimic,
            config = config,
            pluginFullName = description.fullName,
            audiences = checkNotNull(audiences),
        )
        pluginManager.registerEvents(ServicesRegistrationListener(servicesManager, mimic), this)
    }

    @OptIn(ExperimentalMimicApi::class)
    private fun hookDefaultServices() {
        // Default systems
        Log.i(">>> Default systems")
        hookLevels(MinecraftLevelSystem::Provider, priority = Lowest)
        hookClasses(PermissionsClassSystem::Provider, priority = Lowest)
        hookInventory(MinecraftPlayerInventory::Provider, priority = Lowest)
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
        mimic.registerLevelSystem(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
    }

    private fun hookClasses(
        constructor: () -> BukkitClassSystem.Provider,
        priority: ServicePriority = Normal,
    ) {
        mimic.registerClassSystem(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
    }

    @Suppress("SameParameterValue")
    @ExperimentalMimicApi
    private fun hookInventory(
        constructor: () -> BukkitPlayerInventory.Provider,
        priority: ServicePriority = Normal,
    ) {
        mimic.registerPlayerInventoryProvider(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
    }

    private fun hookItems(
        constructor: () -> BukkitItemsRegistry,
        priority: ServicePriority = Normal,
    ) {
        mimic.registerItemsRegistry(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
    }
    //</editor-fold>

    private fun initMetrics() {
        val metrics = Metrics(this, 8413)

        metrics.addCustomChart(SimplePie("level_system") {
            mimic.getLevelSystemProvider().id
        })
        metrics.addCustomChart(SimplePie("class_system") {
            mimic.getClassSystemProvider().id
        })
        metrics.addCustomChart(AdvancedPie("items_registry_custom") {
            servicesManager.loadAll<BukkitItemsRegistry>()
                .map { it.id }
                .filterNot { it == MimicItemsRegistry.ID || it == MinecraftItemsRegistry.ID }
                .associateWith { 1 }
        })
    }

    override fun onDisable() {
        CommandAPI.unregister("mimic")
        CommandAPI.onDisable()
        audiences?.close()
        audiences = null
    }
}
