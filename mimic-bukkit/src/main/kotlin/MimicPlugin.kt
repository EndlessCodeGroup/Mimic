// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic

import org.bstats.bukkit.Metrics
import org.bstats.charts.AdvancedPie
import org.bstats.charts.SimplePie
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.ServicePriority.Lowest
import org.bukkit.plugin.ServicePriority.Normal
import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.mimic.blocks.BukkitBlockRegistry
import ru.endlesscode.mimic.bukkit.register
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.command.registerCommand
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.impl.battlelevels.BattleLevelsLevelSystem
import ru.endlesscode.mimic.impl.craftengine.CraftEngineBlockRegistry
import ru.endlesscode.mimic.impl.craftengine.CraftEngineItemsRegistry
import ru.endlesscode.mimic.impl.customitems.CustomItemsRegistry
import ru.endlesscode.mimic.impl.heroes.HeroesClassSystem
import ru.endlesscode.mimic.impl.heroes.HeroesLevelSystem
import ru.endlesscode.mimic.impl.mimic.PermissionsClassSystem
import ru.endlesscode.mimic.impl.mmocore.MmoCoreClassSystem
import ru.endlesscode.mimic.impl.mmocore.MmoCoreLevelSystem
import ru.endlesscode.mimic.impl.mmoitems.MmoItemsRegistry
import ru.endlesscode.mimic.impl.quantumrpg.QuantumRpgClassSystem
import ru.endlesscode.mimic.impl.quantumrpg.QuantumRpgItemsRegistry
import ru.endlesscode.mimic.impl.quantumrpg.QuantumRpgLevelSystem
import ru.endlesscode.mimic.impl.skillapi.SkillApiClassSystem
import ru.endlesscode.mimic.impl.skillapi.SkillApiLevelSystem
import ru.endlesscode.mimic.impl.vanilla.MinecraftBlockRegistry
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

    private val isReleased = !pluginMeta.version.endsWith("-SNAPSHOT")

    private val config: MimicConfig by lazy { MimicConfig(this) }
    private val mimic: MimicImpl by lazy { MimicImpl(this, config) }

    private inline val servicesManager get() = server.servicesManager
    private inline val pluginManager get() = server.pluginManager

    override fun onLoad() {
        Log.init(logger, debug = !isReleased)

        servicesManager.register<Mimic>(mimic, this)
        hookDefaultServices()
    }

    override fun onEnable() {
        if (isReleased) initMetrics()
        registerCommand(mimic, config)
        pluginManager.registerEvents(mimic, this)

        val servicesRegistrationListener = ServicesRegistrationListener(servicesManager, mimic)
        pluginManager.registerEvents(servicesRegistrationListener, this)
        servicesRegistrationListener.forwardRegisteredServices()
    }

    @OptIn(ExperimentalMimicApi::class)
    private fun hookDefaultServices() {
        // Default systems
        Log.i(">>> Default systems")
        hookLevels(MinecraftLevelSystem::Provider, priority = Lowest)
        hookClasses(PermissionsClassSystem::Provider, priority = Lowest)
        hookInventory(MinecraftPlayerInventory::Provider, priority = Lowest)
        hookItems(::MinecraftItemsRegistry, priority = Lowest)
        hookBlocks(::MinecraftBlockRegistry, priority = Lowest)

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

        pluginHooks("CraftEngine", "net.momirealms.craftengine.bukkit.api.CraftEngineBlocks") {
            hookItems(::CraftEngineItemsRegistry)
            hookBlocks(::CraftEngineBlockRegistry)
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

    @ExperimentalMimicApi
    private fun hookBlocks(
        constructor: () -> BukkitBlockRegistry,
        priority: ServicePriority = Normal,
    ) {
        mimic.registerBlockRegistry(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
    }
    //</editor-fold>

    @OptIn(ExperimentalMimicApi::class)
    private fun initMetrics() {
        val metrics = Metrics(this, 8413)

        metrics.addCustomChart(SimplePie("level_system") {
            mimic.getLevelSystemProvider().id
        })
        metrics.addCustomChart(SimplePie("class_system") {
            mimic.getClassSystemProvider().id
        })
        metrics.addCustomChart(SimplePie("inventory_provider") {
            mimic.getPlayerInventoryProvider().id
        })
        metrics.addCustomChart(AdvancedPie("items_registry_custom") {
            mimic.getAllItemsRegistries().keys
                .filterNot { it == MinecraftItemsRegistry.ID }
                .associateWith { 1 }
        })
        metrics.addCustomChart(AdvancedPie("block_registry_custom") {
            mimic.getAllBlockRegistries().keys
                .filterNot { it == MinecraftBlockRegistry.ID }
                .associateWith { 1 }
        })
    }
}
