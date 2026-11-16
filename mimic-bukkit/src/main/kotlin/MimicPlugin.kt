// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // LevelSystem, ClassSystem and ItemsRegistry APIs are deprecated but still supported

package ru.endlesscode.mimic

import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.ServicePriority.Lowest
import org.bukkit.plugin.ServicePriority.Normal
import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.mimic.blocks.BlockProvider
import ru.endlesscode.mimic.bukkit.register
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.classes.ClassProvider
import ru.endlesscode.mimic.command.registerCommand
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.impl.auraskills.AuraSkillsLevelProvider
import ru.endlesscode.mimic.impl.auraskills.AuraSkillsResourceProvider
import ru.endlesscode.mimic.impl.craftengine.CraftEngineBlockProvider
import ru.endlesscode.mimic.impl.craftengine.CraftEngineItemProvider
import ru.endlesscode.mimic.impl.craftengine.CraftEngineItemsRegistry
import ru.endlesscode.mimic.impl.customitems.CustomItemsItemProvider
import ru.endlesscode.mimic.impl.customitems.CustomItemsRegistry
import ru.endlesscode.mimic.impl.fabled.FabledClassProvider
import ru.endlesscode.mimic.impl.fabled.FabledLevelProvider
import ru.endlesscode.mimic.impl.fabled.FabledResourceProvider
import ru.endlesscode.mimic.impl.heroes.HeroesClassProvider
import ru.endlesscode.mimic.impl.heroes.HeroesClassSystem
import ru.endlesscode.mimic.impl.heroes.HeroesLevelProvider
import ru.endlesscode.mimic.impl.heroes.HeroesLevelSystem
import ru.endlesscode.mimic.impl.heroes.HeroesResourceProvider
import ru.endlesscode.mimic.impl.itemsadder.ItemsAdderBlockProvider
import ru.endlesscode.mimic.impl.itemsadder.ItemsAdderItemProvider
import ru.endlesscode.mimic.impl.jobs.JobsLevelProvider
import ru.endlesscode.mimic.impl.mcmmo.McMmoLevelProvider
import ru.endlesscode.mimic.impl.mimic.PermissionsClassSystem
import ru.endlesscode.mimic.impl.mmocore.MmoCoreClassProvider
import ru.endlesscode.mimic.impl.mmocore.MmoCoreClassSystem
import ru.endlesscode.mimic.impl.mmocore.MmoCoreLevelProvider
import ru.endlesscode.mimic.impl.mmocore.MmoCoreLevelSystem
import ru.endlesscode.mimic.impl.mmocore.MmoCoreResourceProvider
import ru.endlesscode.mimic.impl.mmoitems.MmoItemsItemProvider
import ru.endlesscode.mimic.impl.mmoitems.MmoItemsRegistry
import ru.endlesscode.mimic.impl.nexo.NexoBlockProvider
import ru.endlesscode.mimic.impl.nexo.NexoItemProvider
import ru.endlesscode.mimic.impl.oraxen.OraxenBlockProvider
import ru.endlesscode.mimic.impl.oraxen.OraxenItemProvider
import ru.endlesscode.mimic.impl.valhallammo.ValhallaMmoLevelProvider
import ru.endlesscode.mimic.impl.vanilla.*
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.inventory.BukkitPlayerInventory
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.items.ItemProvider
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.level.LevelProvider
import ru.endlesscode.mimic.resources.ResourceProvider
import ru.endlesscode.mimic.util.checkClassesLoaded

/** Main class of the plugin. */
@Suppress("UnstableApiUsage")
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
        if (isReleased) initMetrics(mimic)
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
        hookLevels(::MinecraftLevelProvider, priority = Lowest)
        hookClasses(PermissionsClassSystem::Provider, priority = Lowest)
        hookResources(::MinecraftResourceProvider, priority = Lowest)
        hookInventory(MinecraftPlayerInventory::Provider, priority = Lowest)
        hookItems(::MinecraftItemProvider, priority = Lowest)
        hookItems(::MinecraftItemsRegistry, priority = Lowest)
        hookBlocks(::MinecraftBlockProvider, priority = Lowest)

        pluginHooks("MMOCore", "net.Indyuce.mmocore.MMOCore") {
            hookLevels(MmoCoreLevelSystem::Provider)
            hookLevels(::MmoCoreLevelProvider)
            hookClasses(MmoCoreClassSystem::Provider)
            hookClasses(::MmoCoreClassProvider)
            hookResources(::MmoCoreResourceProvider)
        }

        pluginHooks("Heroes", "com.herocraftonline.heroes.Heroes") {
            hookLevels(HeroesLevelSystem::Provider)
            hookLevels(::HeroesLevelProvider)
            hookClasses(HeroesClassSystem::Provider)
            hookClasses(::HeroesClassProvider)
            hookResources(::HeroesResourceProvider)
        }

        pluginHooks("CustomItems", "com.jojodmo.customitems.api.CustomItemsAPI") {
            hookItems(::CustomItemsItemProvider)
            hookItems(::CustomItemsRegistry)
        }

        pluginHooks("MMOItems", "net.Indyuce.mmoitems.MMOItems") {
            hookItems(::MmoItemsItemProvider)
            hookItems(::MmoItemsRegistry)
        }

        pluginHooks("CraftEngine", "net.momirealms.craftengine.bukkit.api.CraftEngineBlocks") {
            hookItems(::CraftEngineItemProvider)
            hookItems(::CraftEngineItemsRegistry)
            hookBlocks(::CraftEngineBlockProvider)
        }

        pluginHooks("Oraxen", "io.th0rgal.oraxen.api.OraxenBlocks") {
            hookItems(::OraxenItemProvider)
            hookBlocks(::OraxenBlockProvider)
        }

        pluginHooks("Nexo", "com.nexomc.nexo.api.NexoBlocks") {
            hookItems(::NexoItemProvider)
            hookBlocks(::NexoBlockProvider)
        }

        pluginHooks("AuraSkills", "dev.aurelium.auraskills.api.AuraSkillsApi") {
            hookLevels(::AuraSkillsLevelProvider)
            hookResources(::AuraSkillsResourceProvider)
        }

        pluginHooks("mcMMO", "com.gmail.nossr50.mcMMO") {
            hookLevels(::McMmoLevelProvider)
        }

        pluginHooks("Jobs", "com.gamingmesh.jobs.Jobs") {
            hookLevels(::JobsLevelProvider)
        }

        pluginHooks("Fabled", "studio.magemonkey.fabled.Fabled") {
            hookLevels(::FabledLevelProvider)
            hookClasses(::FabledClassProvider)
            hookResources(::FabledResourceProvider)
        }

        pluginHooks("ValhallaMMO", "me.athlaeos.valhallammo.ValhallaMMO") {
            hookLevels(::ValhallaMmoLevelProvider)
        }

        pluginHooks("ItemsAdder", "dev.lone.itemsadder.api.CustomBlock") {
            hookItems(::ItemsAdderItemProvider)
            hookBlocks(::ItemsAdderBlockProvider)
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
    @JvmName("hookLevel_deprecated")
    private fun hookLevels(
        constructor: () -> BukkitLevelSystem.Provider,
        priority: ServicePriority = Normal,
    ) {
        mimic.registerLevelSystem(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
    }

    private fun hookLevels(
        constructor: () -> LevelProvider,
        priority: ServicePriority = Normal,
    ) {
        mimic.registerLevelProvider(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
    }

    @JvmName("hookClasses_deprecated")
    private fun hookClasses(
        constructor: () -> BukkitClassSystem.Provider,
        priority: ServicePriority = Normal,
    ) {
        mimic.registerClassSystem(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
    }

    private fun hookClasses(
        constructor: () -> ClassProvider,
        priority: ServicePriority = Normal,
    ) {
        mimic.registerClassProvider(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
    }

    private fun hookResources(
        constructor: () -> ResourceProvider,
        priority: ServicePriority = Normal,
    ) {
        mimic.registerResourceProvider(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
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
        constructor: () -> ItemProvider,
        priority: ServicePriority = Normal,
    ) {
        mimic.registerItemProvider(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
    }

    @JvmName("hookItems_deprecated")
    private fun hookItems(
        constructor: () -> BukkitItemsRegistry,
        priority: ServicePriority = Normal,
    ) {
        mimic.registerItemsRegistry(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
    }

    private fun hookBlocks(
        constructor: () -> BlockProvider,
        priority: ServicePriority = Normal,
    ) {
        mimic.registerBlockProvider(constructor.invoke(), MimicApiLevel.CURRENT, this, priority)
    }
    //</editor-fold>
}
