package ru.endlesscode.mimic

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicesManager
import ru.endlesscode.mimic.bukkit.loadAll
import ru.endlesscode.mimic.bukkit.register
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.level.BukkitLevelSystem
import java.lang.reflect.Constructor

internal class MimicImpl(private val servicesManager: ServicesManager) : Mimic {

    override fun registerClassSystem(classSystemClass: Class<out BukkitClassSystem>, plugin: Plugin) {
        val constructor = classSystemClass.getPlayerConstructor()
        registerClassSystem(DefaultClassSystemProvider(plugin, constructor), plugin)
    }

    override fun registerClassSystem(provider: BukkitClassSystem.Provider, plugin: Plugin) {
        servicesManager.register(provider, plugin)
    }

    override fun getClassSystem(player: Player): BukkitClassSystem = getClassSystemProvider().getSystem(player)

    override fun getClassSystemProvider(): BukkitClassSystem.Provider = loadService()

    override fun registerItemsRegistry(registry: BukkitItemsRegistry, plugin: Plugin) {
        servicesManager.register(registry, plugin)
    }

    override fun getItemsRegistry(): BukkitItemsRegistry = loadService()

    override fun registerLevelSystem(levelSystemClass: Class<out BukkitLevelSystem>, plugin: Plugin) {
        val constructor = levelSystemClass.getPlayerConstructor()
        registerLevelSystem(DefaultLevelSystemProvider(plugin, constructor), plugin)
    }

    override fun registerLevelSystem(provider: BukkitLevelSystem.Provider, plugin: Plugin) {
        servicesManager.register(provider, plugin)
    }

    override fun getLevelSystem(player: Player): BukkitLevelSystem = getLevelSystemProvider().getSystem(player)

    override fun getLevelSystemProvider(): BukkitLevelSystem.Provider = loadService()

    private fun <T : Any> Class<T>.getPlayerConstructor(): Constructor<T> {
        return try {
            getConstructor(Player::class.java)
        } catch (exception: NoSuchMethodException) {
            error(
                """
                $simpleName have not constructor with single parameter of type Player.
                Please implement provider for it manually.
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
            """
        }
    }

    companion object {
        fun register(servicesManager: ServicesManager, plugin: Plugin) {
            servicesManager.register<Mimic>(MimicImpl(servicesManager), plugin)
        }
    }
}
