package ru.endlesscode.mimic

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.ServicesManager
import ru.endlesscode.mimic.bukkit.loadAll
import ru.endlesscode.mimic.bukkit.register
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.classes.ClassSystem
import ru.endlesscode.mimic.classes.WrappedClassSystemProvider
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.items.ItemsRegistry
import ru.endlesscode.mimic.items.WrappedItemsRegistry
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.level.LevelSystem
import ru.endlesscode.mimic.level.WrappedLevelSystemProvider

internal class MimicImpl(private val servicesManager: ServicesManager) : Mimic {

    override fun registerClassSystem(
        provider: BukkitClassSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitClassSystem.Provider? {
        if (!validateApiLevel<ClassSystem>(apiLevel, plugin)) return null
        val wrappedProvider = WrappedClassSystemProvider(provider, plugin)
        servicesManager.register(wrappedProvider, plugin, priority)
        return wrappedProvider
    }

    override fun getClassSystem(player: Player): BukkitClassSystem = getClassSystemProvider().getSystem(player)

    override fun getClassSystemProvider(): BukkitClassSystem.Provider = loadService()

    override fun registerItemsRegistry(
        registry: BukkitItemsRegistry,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitItemsRegistry? {
        if (!validateApiLevel<ItemsRegistry<*>>(apiLevel, plugin)) return null
        val wrappedRegistry = WrappedItemsRegistry(registry, plugin)
        servicesManager.register(wrappedRegistry, plugin, priority)
        return wrappedRegistry
    }

    override fun getItemsRegistry(): BukkitItemsRegistry = loadService()

    override fun registerLevelSystem(
        provider: BukkitLevelSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitLevelSystem.Provider? {
        if (!validateApiLevel<LevelSystem>(apiLevel, plugin)) return null
        val wrappedProvider = WrappedLevelSystemProvider(provider, plugin)
        servicesManager.register(wrappedProvider, plugin, priority)
        return wrappedProvider
    }

    override fun getLevelSystem(player: Player): BukkitLevelSystem = getLevelSystemProvider().getSystem(player)

    override fun getLevelSystemProvider(): BukkitLevelSystem.Provider = loadService()

    private inline fun <reified T : Any> validateApiLevel(apiLevel: Int, plugin: Plugin): Boolean {
        if (MimicApiLevel.checkApiLevel(apiLevel)) return true

        Log.w(
            "Can not register ${T::class.simpleName} implemented via ${plugin.name},",
            "because Mimic version installed on the server is lower than minimal required version.",
            "You can get the latest Mimic from GitHub: https://github.com/EndlessCodeGroup/Mimic/releases",
        )
        return false
    }

    private inline fun <reified T : MimicService> loadService(): T {
        val service = servicesManager.loadAll<T>()
            .firstOrNull { it.isEnabled }

        return checkNotNull(service) {
            """
            ${T::class.simpleName} should always have default implementation.
            Please file an issue on GitHub: https://github.com/EndlessCodeGroup/Mimic/issues
            """
        }
    }
}
