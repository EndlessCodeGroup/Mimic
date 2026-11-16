// Copyright (c) 2022 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION") // LevelSystem and ClassSystem APIs are deprecated but still supported

package ru.endlesscode.mimic

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import ru.endlesscode.mimic.blocks.BlockProvider
import ru.endlesscode.mimic.blocks.Blocks
import ru.endlesscode.mimic.blocks.WrappedBlockProvider
import ru.endlesscode.mimic.classes.*
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.impl.mimic.MimicBlocks
import ru.endlesscode.mimic.impl.mimic.MimicClasses
import ru.endlesscode.mimic.impl.mimic.MimicItemsRegistry
import ru.endlesscode.mimic.impl.mimic.MimicLevels
import ru.endlesscode.mimic.impl.mimic.MimicResources
import ru.endlesscode.mimic.impl.mimic.PermissionsClassSystem
import ru.endlesscode.mimic.impl.vanilla.*
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.authorsMention
import ru.endlesscode.mimic.inventory.WrappedPlayerInventoryProvider
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.items.WrappedItemsRegistry
import ru.endlesscode.mimic.level.*
import ru.endlesscode.mimic.resources.ResourceProvider
import ru.endlesscode.mimic.resources.Resources
import ru.endlesscode.mimic.resources.WrappedResourceProvider
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

    val blockProviders = registry<BlockProvider>(
        "BlockProvider",
        defaultId = MinecraftBlockProvider.ID,
        disabledIds = { config.disabledBlockProviders },
    )
    private val classSystems = registry<BukkitClassSystem.Provider>(
        "ClassSystem",
        defaultId = PermissionsClassSystem.ID,
    )
    // There is no default class provider, the ID is only reserved, see registerClassSystem
    val classProviders = registry<ClassProvider>(
        "ClassProvider",
        defaultId = PermissionsClassSystem.ID,
    )
    private val playerInventories = registry<PlayerInventoryProvider>(
        "PlayerInventory",
        defaultId = MinecraftPlayerInventory.ID,
    )
    val itemsRegistries = registry<BukkitItemsRegistry>(
        "ItemsRegistry",
        defaultId = MinecraftItemsRegistry.ID,
        disabledIds = { config.disabledItemsRegistries },
    )
    private val levelSystems = registry<BukkitLevelSystem.Provider>(
        "LevelSystem",
        defaultId = MinecraftLevelSystem.ID,
    )
    val levelProviders = registry<LevelProvider>(
        "LevelProvider",
        defaultId = MinecraftLevelProvider.ID,
    )
    val resourceProviders = registry<ResourceProvider>(
        "ResourceProvider",
        defaultId = MinecraftResourceProvider.ID,
    )

    private val registries =
        listOf(
            blockProviders,
            classSystems,
            classProviders,
            playerInventories,
            itemsRegistries,
            levelSystems,
            levelProviders,
            resourceProviders,
        )

    private fun <T : MimicService> registry(
        apiName: String,
        defaultId: String,
        disabledIds: () -> Set<String> = { emptySet() },
    ): ServiceRegistry<T> = ServiceRegistry<T>(apiName, mimicPlugin, defaultId, disabledIds)

    // region Class System
    override fun registerClassSystem(
        provider: BukkitClassSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitClassSystem.Provider? {
        val classSystem = classSystems.tryRegister(apiLevel, plugin, priority) {
            WrappedClassSystemProvider(provider, plugin)
        }
        // Permissions class system isn't bridged: it can't list its classes,
        // and a player may have several classes while a slot holds only one.
        if (classSystem != null && classSystem.id != PermissionsClassSystem.ID) {
            classProviders.registerBridge(ClassSystemSlotProvider(classSystem), plugin, priority) {
                "ClassSystem '${classSystem.id}' by $plugin uses deprecated ClassSystem API, " +
                    "so it is available via Classes API as the single slot '${classSystem.id}:class'. " +
                    "Please ask ${plugin.authorsMention} to migrate to ClassProvider."
            }
        }
        return classSystem
    }

    override fun getClassSystemProvider(): BukkitClassSystem.Provider =
        classSystems.loadPreferredOrDefault(config.classProvider, newApiRegistry = classProviders)

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
        if (levelSystem != null) {
            levelProviders.registerBridge(LevelSystemTrackProvider(levelSystem), plugin, priority) {
                "LevelSystem '${levelSystem.id}' by $plugin uses deprecated LevelSystem API, " +
                    "so it is available via Levels API as the single track '${levelSystem.id}:level'. " +
                    "Please ask ${plugin.authorsMention} to migrate to LevelProvider."
            }
        }
        return levelSystem
    }

    override fun getLevelSystemProvider(): BukkitLevelSystem.Provider =
        levelSystems.loadPreferredOrDefault(config.levelProvider, newApiRegistry = levelProviders)

    override fun getAllLevelSystemProviders(): Map<String, BukkitLevelSystem.Provider> = levelSystems.loadAll()
    // endregion

    // region Levels
    override fun registerLevelProvider(
        provider: LevelProvider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): LevelProvider? = levelProviders.tryRegister(apiLevel, plugin, priority) {
        WrappedLevelProvider(provider, plugin)
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

    // The 'permissions' value selects the class system of the deprecated API, there is no such class provider
    override val classes: Classes = MimicClasses(
        classProviders,
        preferredProviderId = { config.classProvider.takeUnless { it == PermissionsClassSystem.ID }.orEmpty() },
    )
    // endregion

    // region Resources
    override fun registerResourceProvider(
        provider: ResourceProvider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): ResourceProvider? = resourceProviders.tryRegister(apiLevel, plugin, priority) {
        WrappedResourceProvider(provider, plugin)
    }

    override val resources: Resources =
        MimicResources(resourceProviders, preferredProviderId = { config.resourceProvider })
    // endregion

    // region Blocks
    override fun registerBlockProvider(
        provider: BlockProvider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BlockProvider? = blockProviders.tryRegister(apiLevel, plugin, priority) {
        WrappedBlockProvider(provider, plugin)
    }

    override val blocks: Blocks = MimicBlocks(blockProviders)
    // endregion

    // region Bridges from deprecated APIs
    /**
     * Registers the given [bridge] exposing a service of a deprecated API via the new API.
     * A service of the new API with the same ID always takes precedence over it, see [ServiceRegistry].
     * Logs the [deprecationMessage] if the bridge is registered and isn't built into Mimic.
     */
    private fun <T : MimicService> ServiceRegistry<T>.registerBridge(
        bridge: T,
        plugin: Plugin,
        priority: ServicePriority,
        deprecationMessage: () -> String,
    ) {
        if (register(bridge, plugin, priority, isBridge = true) && plugin != mimicPlugin) Log.w(deprecationMessage())
    }

    /**
     * Loads the [preferred] service of a deprecated API.
     * The preferred service may be available only via the new API, then the default service is loaded quietly.
     */
    private fun <T : MimicService> ServiceRegistry<T>.loadPreferredOrDefault(
        preferred: String,
        newApiRegistry: ServiceRegistry<*>,
    ): T {
        if (preferred.isEmpty()) return load(preferred)
        val isNewApiOnly = enabledEntries.none { it.service.id == preferred } &&
            newApiRegistry.enabledEntries.any { it.service.id == preferred }
        return load(if (isNewApiOnly) "" else preferred)
    }
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
