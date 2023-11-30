package ru.endlesscode.mimic

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.ServicesManager
import ru.endlesscode.mimic.blocks.BukkitBlocksRegistry
import ru.endlesscode.mimic.bukkit.loadAll
import ru.endlesscode.mimic.bukkit.register
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.classes.WrappedClassSystemProvider
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.inventory.BukkitPlayerInventory
import ru.endlesscode.mimic.inventory.WrappedPlayerInventoryProvider
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.items.WrappedItemsRegistry
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.level.WrappedLevelSystemProvider
import kotlin.reflect.KClass
import ru.endlesscode.mimic.inventory.BukkitPlayerInventory.Provider as PlayerInventoryProvider

internal class MimicImpl(
    private val servicesManager: ServicesManager,
    private val config: MimicConfig,
) : Mimic {

    // region Blocks Registry
    @ExperimentalMimicApi
    override fun registerBlocksRegistry(
        registry: BukkitBlocksRegistry,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority
    ): BukkitBlocksRegistry? = tryRegisterService<BukkitBlocksRegistry>(apiLevel, plugin, priority) {
        TODO("Not implemented yet")
        // WrappedBlocksRegistry(registry, config, plugin)
    }

    @ExperimentalMimicApi
    override fun getBlocksRegistry(): BukkitBlocksRegistry = loadService()

    @ExperimentalMimicApi
    override fun getAllBlocksRegistries(): Map<String, BukkitBlocksRegistry> = loadAllServices()
    // endregion

    // region Class System
    override fun registerClassSystem(
        provider: BukkitClassSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitClassSystem.Provider? = tryRegisterService<BukkitClassSystem.Provider>(apiLevel, plugin, priority) {
        WrappedClassSystemProvider(provider, plugin)
    }

    override fun getClassSystemProvider(): BukkitClassSystem.Provider = loadService(config.classSystem)
    override fun getAllClassSystemProviders(): Map<String, BukkitClassSystem.Provider> = loadAllServices()
    // endregion

    // region Inventory Provider
    @ExperimentalMimicApi
    override fun registerPlayerInventoryProvider(
        provider: PlayerInventoryProvider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): PlayerInventoryProvider? = tryRegisterService<PlayerInventoryProvider>(apiLevel, plugin, priority) {
        WrappedPlayerInventoryProvider(provider, plugin)
    }

    @ExperimentalMimicApi
    override fun getPlayerInventoryProvider(): PlayerInventoryProvider = loadService(config.inventoryProvider)

    @ExperimentalMimicApi
    override fun getAllPlayerInventoryProviders(): Map<String, PlayerInventoryProvider> = loadAllServices()
    // endregion

    // region Items Registry
    override fun registerItemsRegistry(
        registry: BukkitItemsRegistry,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitItemsRegistry? = tryRegisterService<BukkitItemsRegistry>(apiLevel, plugin, priority) {
        WrappedItemsRegistry(registry, config, plugin)
    }

    override fun getItemsRegistry(): BukkitItemsRegistry = loadService()
    override fun getAllItemsRegistries(): Map<String, BukkitItemsRegistry> = loadAllServices()
    // endregion

    // region Level System
    override fun registerLevelSystem(
        provider: BukkitLevelSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitLevelSystem.Provider? = tryRegisterService<BukkitLevelSystem.Provider>(apiLevel, plugin, priority) {
        WrappedLevelSystemProvider(provider, plugin)
    }

    override fun getLevelSystemProvider(): BukkitLevelSystem.Provider = loadService(config.levelSystem)
    override fun getAllLevelSystemProviders(): Map<String, BukkitLevelSystem.Provider> = loadAllServices()
    // endregion

    private inline fun <reified Service : MimicService> tryRegisterService(
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
        crossinline createService: () -> Service,
    ): Service? = runCatching {
        // Check if service can be registered
        val apiName = Service::class.getApiName()
        checkApiLevel(apiLevel, apiName, plugin)

        // Register service
        val service = createService()
        servicesManager.register(service, plugin, priority)
        Log.i("Successfully registered $apiName '${service.id}' by $plugin")

        // Return the registered service or null if service is not registered
        service
    }.onFailure(Log::w).getOrNull()

    private fun checkApiLevel(apiLevel: Int, apiName: String, plugin: Plugin) {
        if (!MimicApiLevel.checkApiLevel(apiLevel)) {
            error(
                """
                Can not register $apiName implemented by $plugin,
                because Mimic version installed on the server is lower than minimal required version.
                You can get the latest Mimic from GitHub: https://github.com/EndlessCodeGroup/Mimic/releases
                """.trimIndent()
            )
        }
    }

    private inline fun <reified T : MimicService> loadService(preferred: String = ""): T {
        val services = loadAllServices<T>().filterValues { it.isEnabled }
        val defaultService = services.values.firstOrNull()
        checkNotNull(defaultService) {
            """
            ${T::class.getApiName()} should always have default implementation.
            Please file an issue on GitHub: https://github.com/EndlessCodeGroup/Mimic/issues
            """.trimIndent()
        }

        return when {
            preferred.isEmpty() -> defaultService
            preferred in services -> services.getValue(preferred)

            else -> {
                Log.w("${T::class.getApiName()} with id '$preferred' not found, will be used '${defaultService.id}' instead.")
                Log.w("Please specify any of known implementations: ${services.keys}.")
                defaultService
            }
        }
    }

    private inline fun <reified T : MimicService> loadAllServices(): Map<String, T> {
        return servicesManager.loadAll<T>().associateBy { it.id }
    }

    @OptIn(ExperimentalMimicApi::class)
    private fun KClass<out MimicService>.getApiName(): String = when (this) {
        BukkitClassSystem.Provider::class -> "ClassSystem"
        BukkitLevelSystem.Provider::class -> "LevelSystem"
        BukkitPlayerInventory.Provider::class -> "PlayerInventory"
        BukkitItemsRegistry::class -> "ItemsRegistry"
        BukkitBlocksRegistry::class -> "BlocksRegistry"
        else -> error("Unknown service: ${this.java.name}")
    }
}
