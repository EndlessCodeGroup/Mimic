package ru.endlesscode.mimic

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.ServicesManager
import ru.endlesscode.mimic.bukkit.loadAll
import ru.endlesscode.mimic.bukkit.register
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.classes.WrappedClassSystemProvider
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.items.WrappedItemsRegistry
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.level.WrappedLevelSystemProvider
import kotlin.reflect.KClass

internal class MimicImpl(private val servicesManager: ServicesManager) : Mimic {

    override fun registerClassSystem(
        provider: BukkitClassSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitClassSystem.Provider? = tryRegisterService<BukkitClassSystem.Provider>(apiLevel, plugin, priority) {
        WrappedClassSystemProvider(provider, plugin)
    }

    override fun getClassSystem(player: Player): BukkitClassSystem = getClassSystemProvider().getSystem(player)

    override fun getClassSystemProvider(): BukkitClassSystem.Provider = loadService()

    override fun registerItemsRegistry(
        registry: BukkitItemsRegistry,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitItemsRegistry? = tryRegisterService<BukkitItemsRegistry>(apiLevel, plugin, priority) {
        WrappedItemsRegistry(registry, plugin)
    }

    override fun getItemsRegistry(): BukkitItemsRegistry = loadService()

    override fun registerLevelSystem(
        provider: BukkitLevelSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitLevelSystem.Provider? = tryRegisterService<BukkitLevelSystem.Provider>(apiLevel, plugin, priority) {
        WrappedLevelSystemProvider(provider, plugin)
    }

    override fun getLevelSystem(player: Player): BukkitLevelSystem = getLevelSystemProvider().getSystem(player)

    override fun getLevelSystemProvider(): BukkitLevelSystem.Provider = loadService()

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

    private fun KClass<out MimicService>.getApiName(): String = when (this) {
        BukkitClassSystem.Provider::class -> "ClassSystem"
        BukkitLevelSystem.Provider::class -> "LevelSystem"
        BukkitItemsRegistry::class -> "ItemsRegistry"
        else -> error("Unknown service: ${this.java.name}")
    }

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

    private inline fun <reified T : MimicService> loadService(): T {
        val service = servicesManager.loadAll<T>()
            .firstOrNull { it.isEnabled }

        return checkNotNull(service) {
            """
            ${T::class.simpleName} should always have default implementation.
            Please file an issue on GitHub: https://github.com/EndlessCodeGroup/Mimic/issues
            """.trimIndent()
        }
    }
}
