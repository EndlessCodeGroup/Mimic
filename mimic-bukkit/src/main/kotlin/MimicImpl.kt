package ru.endlesscode.mimic

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import ru.endlesscode.mimic.blocks.BukkitBlockRegistry
import ru.endlesscode.mimic.blocks.WrappedBlockRegistry
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.classes.WrappedClassSystemProvider
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.impl.mimic.MimicBlockRegistry
import ru.endlesscode.mimic.impl.mimic.MimicItemsRegistry
import ru.endlesscode.mimic.impl.mimic.PermissionsClassSystem
import ru.endlesscode.mimic.impl.vanilla.MinecraftBlockRegistry
import ru.endlesscode.mimic.impl.vanilla.MinecraftItemsRegistry
import ru.endlesscode.mimic.impl.vanilla.MinecraftLevelSystem
import ru.endlesscode.mimic.impl.vanilla.MinecraftPlayerInventory
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.parseKey
import ru.endlesscode.mimic.inventory.WrappedPlayerInventoryProvider
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.items.WrappedItemsRegistry
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.level.WrappedLevelSystemProvider
import ru.endlesscode.mimic.inventory.BukkitPlayerInventory.Provider as PlayerInventoryProvider

/**
 * Stores implementations of Mimic APIs.
 *
 * IDs of Mimic default implementations are reserved, so other plugins can't replace them.
 */
@OptIn(ExperimentalMimicApi::class)
internal class MimicImpl(
    mimicPlugin: Plugin,
    private val config: MimicConfig,
) : Mimic, Listener {

    private val blockRegistries = ServiceRegistry<BukkitBlockRegistry>(
        "BlockRegistry", mimicPlugin, reservedIds = setOf(MinecraftBlockRegistry.ID, MimicBlockRegistry.ID),
    )
    private val classSystems = ServiceRegistry<BukkitClassSystem.Provider>(
        "ClassSystem", mimicPlugin, reservedIds = setOf(PermissionsClassSystem.ID),
    )
    private val playerInventories = ServiceRegistry<PlayerInventoryProvider>(
        "PlayerInventory", mimicPlugin, reservedIds = setOf(MinecraftPlayerInventory.ID),
    )
    private val itemsRegistries = ServiceRegistry<BukkitItemsRegistry>(
        "ItemsRegistry", mimicPlugin, reservedIds = setOf(MinecraftItemsRegistry.ID, MimicItemsRegistry.ID),
    )
    private val levelSystems = ServiceRegistry<BukkitLevelSystem.Provider>(
        "LevelSystem", mimicPlugin, reservedIds = setOf(MinecraftLevelSystem.ID),
    )

    private val blockRegistry = MimicBlockRegistry(blockRegistries, parseKey = mimicPlugin::parseKey)
    private val itemsRegistry = MimicItemsRegistry(itemsRegistries)

    // region Block Registry
    @ExperimentalMimicApi
    override fun registerBlockRegistry(
        registry: BukkitBlockRegistry,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority
    ): BukkitBlockRegistry? = blockRegistries.tryRegister(apiLevel, plugin, priority) {
        WrappedBlockRegistry(registry, config, plugin)
    }

    @ExperimentalMimicApi
    override fun getBlockRegistry(): BukkitBlockRegistry = blockRegistry

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
    override fun getPlayerInventoryProvider(): PlayerInventoryProvider = playerInventories.load(config.inventoryProvider)

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
        WrappedItemsRegistry(registry, config, plugin)
    }

    override fun getItemsRegistry(): BukkitItemsRegistry = itemsRegistry
    override fun getAllItemsRegistries(): Map<String, BukkitItemsRegistry> = itemsRegistries.loadAll()
    // endregion

    // region Level System
    override fun registerLevelSystem(
        provider: BukkitLevelSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitLevelSystem.Provider? = levelSystems.tryRegister(apiLevel, plugin, priority) {
        WrappedLevelSystemProvider(provider, plugin)
    }

    override fun getLevelSystemProvider(): BukkitLevelSystem.Provider = levelSystems.load(config.levelSystem)
    override fun getAllLevelSystemProviders(): Map<String, BukkitLevelSystem.Provider> = levelSystems.loadAll()
    // endregion

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        blockRegistries.unregisterAll(event.plugin)
        classSystems.unregisterAll(event.plugin)
        playerInventories.unregisterAll(event.plugin)
        itemsRegistries.unregisterAll(event.plugin)
        levelSystems.unregisterAll(event.plugin)
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
        val services = entries.mapNotNull { it.service.takeIf(MimicService::isEnabled) }
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
