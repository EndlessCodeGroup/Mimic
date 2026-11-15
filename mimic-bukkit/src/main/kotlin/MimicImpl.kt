// Copyright (c) 2022 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION") // LevelSystem and ClassSystem APIs are deprecated but still supported

package ru.endlesscode.mimic

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import ru.endlesscode.mimic.blocks.BukkitBlockRegistry
import ru.endlesscode.mimic.blocks.WrappedBlockRegistry
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.classes.ClassProvider
import ru.endlesscode.mimic.classes.Classes
import ru.endlesscode.mimic.classes.WrappedClassProvider
import ru.endlesscode.mimic.classes.WrappedClassSystemProvider
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.impl.mimic.MimicBlockRegistry
import ru.endlesscode.mimic.impl.mimic.MimicClasses
import ru.endlesscode.mimic.impl.mimic.MimicItemsRegistry
import ru.endlesscode.mimic.impl.mimic.MimicLevels
import ru.endlesscode.mimic.impl.mimic.PermissionsClassSystem
import ru.endlesscode.mimic.impl.vanilla.*
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.authorsMention
import ru.endlesscode.mimic.inventory.WrappedPlayerInventoryProvider
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.items.WrappedItemsRegistry
import ru.endlesscode.mimic.level.*
import ru.endlesscode.mimic.inventory.BukkitPlayerInventory.Provider as PlayerInventoryProvider

/**
 * Stores implementations of Mimic APIs.
 *
 * IDs of Mimic default implementations are reserved, so other plugins can't replace them.
 */
@OptIn(ExperimentalMimicApi::class)
internal class MimicImpl(
    private val mimicPlugin: Plugin,
    private val config: MimicConfig,
) : Mimic, Listener {

    private val blockRegistries = registry<BukkitBlockRegistry>(
        "BlockRegistry",
        defaultId = MinecraftBlockRegistry.ID,
        disabledIds = { config.disabledBlockRegistries },
    )
    private val classSystems = registry<BukkitClassSystem.Provider>(
        "ClassSystem",
        defaultId = PermissionsClassSystem.ID,
    )
    private val playerInventories = registry<PlayerInventoryProvider>(
        "PlayerInventory",
        defaultId = MinecraftPlayerInventory.ID,
    )
    private val itemsRegistries = registry<BukkitItemsRegistry>(
        "ItemsRegistry",
        defaultId = MinecraftItemsRegistry.ID,
        disabledIds = { config.disabledItemsRegistries },
    )
    private val levelSystems = registry<BukkitLevelSystem.Provider>(
        "LevelSystem",
        defaultId = MinecraftLevelSystem.ID,
    )
    private val classProviders = registry<ClassProvider>(
        "ClassProvider",
        defaultId = PermissionsClassSystem.ID,
    )
    private val levelProviders = registry<LevelProvider>(
        "LevelProvider",
        defaultId = MinecraftLevelProvider.ID,
    )

    private val registries =
        listOf(
            blockRegistries,
            classSystems,
            classProviders,
            playerInventories,
            itemsRegistries,
            levelSystems,
            levelProviders,
        )

    private fun <T : MimicService> registry(
        apiName: String,
        defaultId: String,
        disabledIds: () -> Set<String> = { emptySet() },
    ): ServiceRegistry<T> = ServiceRegistry<T>(apiName, mimicPlugin, defaultId, disabledIds)

    // region Block Registry
    @ExperimentalMimicApi
    override fun registerBlockRegistry(
        registry: BukkitBlockRegistry,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority
    ): BukkitBlockRegistry? = blockRegistries.tryRegister(apiLevel, plugin, priority) {
        WrappedBlockRegistry(registry, plugin)
    }

    @ExperimentalMimicApi
    override val blockRegistry: BukkitBlockRegistry = MimicBlockRegistry(blockRegistries)

    @ExperimentalMimicApi
    override fun getAllBlockRegistries(): Map<String, BukkitBlockRegistry> = blockRegistries.loadAll()
    // endregion

    // region Class System
    override fun registerClassSystem(
        provider: BukkitClassSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitClassSystem.Provider? = classSystems.tryRegister(apiLevel, plugin, priority) {
        WrappedClassSystemProvider(provider, plugin)
    }

    override fun getClassSystemProvider(): BukkitClassSystem.Provider = classSystems.load(config.classSystem)

    override fun getAllClassSystemProviders(): Map<String, BukkitClassSystem.Provider> = classSystems.loadAll()
    // endregion

    // region Inventory Provider
    @ExperimentalMimicApi
    override fun registerPlayerInventoryProvider(
        provider: PlayerInventoryProvider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): PlayerInventoryProvider? = playerInventories.tryRegister(apiLevel, plugin, priority) {
        WrappedPlayerInventoryProvider(provider, plugin)
    }

    @ExperimentalMimicApi
    override fun getPlayerInventoryProvider(): PlayerInventoryProvider =
        playerInventories.load(config.inventoryProvider)

    @ExperimentalMimicApi
    override fun getAllPlayerInventoryProviders(): Map<String, PlayerInventoryProvider> = playerInventories.loadAll()
    // endregion

    // region Items Registry
    override fun registerItemsRegistry(
        registry: BukkitItemsRegistry,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitItemsRegistry? = itemsRegistries.tryRegister(apiLevel, plugin, priority) {
        WrappedItemsRegistry(registry, plugin)
    }

    override val itemsRegistry: BukkitItemsRegistry = MimicItemsRegistry(itemsRegistries)
    override fun getAllItemsRegistries(): Map<String, BukkitItemsRegistry> = itemsRegistries.loadAll()
    // endregion

    // region Level System
    override fun registerLevelSystem(
        provider: BukkitLevelSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitLevelSystem.Provider? {
        val levelSystem = levelSystems.tryRegister(apiLevel, plugin, priority) {
            WrappedLevelSystemProvider(provider, plugin)
        }
        if (levelSystem != null) bridgeLevelSystem(levelSystem, plugin, priority)
        return levelSystem
    }

    /** Makes the given [levelSystem] available via [levels], unless a level provider with the same ID is registered. */
    private fun bridgeLevelSystem(levelSystem: BukkitLevelSystem.Provider, plugin: Plugin, priority: ServicePriority) {
        val hasLevelProvider = levelProviders.entries.any { (service) ->
            service.id == levelSystem.id && service !is LevelSystemTrackProvider
        }
        if (hasLevelProvider) return

        levelProviders.register(LevelSystemTrackProvider(levelSystem), plugin, priority)
        if (plugin != mimicPlugin) {
            Log.w(
                "LevelSystem '${levelSystem.id}' by $plugin uses deprecated LevelSystem API, " +
                    "so it is available via Levels API as the single track '${levelSystem.id}:level'. " +
                    "Please ask ${plugin.authorsMention} to migrate to LevelProvider."
            )
        }
    }

    override fun getLevelSystemProvider(): BukkitLevelSystem.Provider {
        // The preferred level system may be a level provider not available via LevelSystem API
        val preferred = config.levelProvider
        val isLevelProviderOnly = levelProviders.enabledEntries.any { it.service.id == preferred } &&
                levelSystems.enabledEntries.none { it.service.id == preferred }
        return levelSystems.load(if (isLevelProviderOnly) "" else preferred)
    }

    override fun getAllLevelSystemProviders(): Map<String, BukkitLevelSystem.Provider> = levelSystems.loadAll()
    // endregion

    // region Levels
    override fun registerLevelProvider(
        provider: LevelProvider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): LevelProvider? = levelProviders.tryRegister(apiLevel, plugin, priority) {
        WrappedLevelProvider(provider, plugin).also { unregisterBridgedLevelSystem(it.id) }
    }

    /** Unregisters the level system bridged with the given [id], so a level provider always replaces it. */
    private fun unregisterBridgedLevelSystem(id: String) {
        val bridge = levelProviders.entries.find { (service) -> service.id == id && service is LevelSystemTrackProvider }
        if (bridge != null) levelProviders.unregister(bridge.service)
    }

    override val levels: Levels =
        MimicLevels(levelProviders, preferredProviderId = { config.levelProvider })
    // endregion

    // region Classes
    override fun registerClassProvider(
        provider: ClassProvider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): ClassProvider? = classProviders.tryRegister(apiLevel, plugin, priority) {
        WrappedClassProvider(provider, plugin)
    }

    override val classes: Classes =
        MimicClasses(classProviders, preferredProviderId = { config.classSystem })
    // endregion

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        registries.forEach { it.unregisterAll(event.plugin) }
    }

    private inline fun <Service : MimicService> ServiceRegistry<Service>.tryRegister(
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
        createService: () -> Service,
    ): Service? = runCatching {
        // Check if service can be registered
        checkApiLevel(apiLevel, apiName, plugin)

        // Register service
        val service = createService()
        if (register(service, plugin, priority)) {
            Log.i("Successfully registered $apiName '${service.id}' by $plugin")
            service
        } else {
            null
        }
    }.onFailure(Log::w).getOrNull()

    private fun checkApiLevel(apiLevel: Int, apiName: String, plugin: Plugin) {
        if (!MimicApiLevel.checkApiLevel(apiLevel)) {
            error(
                """
                Can not register $apiName implemented by $plugin,
                because Mimic version installed on the server is lower than the minimal required version.
                You can get the latest Mimic from GitHub: https://github.com/EndlessCodeGroup/Mimic/releases
                """.trimIndent()
            )
        }
    }

    private fun <T : MimicService> ServiceRegistry<T>.load(preferred: String): T {
        val services = enabledEntries.map { it.service }
        val defaultService = services.firstOrNull()
        checkNotNull(defaultService) {
            """
            $apiName should always have default implementation.
            Please file an issue on GitHub: https://github.com/EndlessCodeGroup/Mimic/issues
            """.trimIndent()
        }

        if (preferred.isEmpty()) return defaultService
        return services.find { it.id == preferred } ?: run {
            Log.w("$apiName with id '$preferred' not found, will be used '${defaultService.id}' instead.")
            Log.w("Please specify any of known implementations: ${services.map { it.id }}.")
            defaultService
        }
    }

    private fun <T : MimicService> ServiceRegistry<T>.loadAll(): Map<String, T> =
        entries.associate { it.service.id to it.service }
}
