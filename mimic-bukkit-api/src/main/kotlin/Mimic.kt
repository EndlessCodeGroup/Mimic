package ru.endlesscode.mimic

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.bukkit.load
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.level.BukkitLevelSystem

/** Mimic provides access to abstraction APIs. */
public interface Mimic {

    /**
     * Registers [BukkitClassSystem] implemented via the given [plugin]
     * using the default [BukkitClassSystem.Provider] implementation.
     *
     * Default provider implementation can be used only if the given
     * [classSystemClass] has constructor with single parameter of type [Player].
     * If you need more parameters for system initialization,
     * you should implement provider manually.
     */
    public fun registerClassSystem(classSystemClass: Class<out BukkitClassSystem>, plugin: Plugin)

    /** Registers the given [provider] for [BukkitClassSystem] implemented via the given [plugin]. */
    public fun registerClassSystem(provider: BukkitClassSystem.Provider, plugin: Plugin)

    /**
     * Returns top priority [BukkitClassSystem] for the given [player].
     * It is a shorthand for `getClassSystemProvider().getSystem(player)`.
     */
    public fun getClassSystem(player: Player): BukkitClassSystem

    /** Returns top priority [BukkitClassSystem.Provider]. */
    public fun getClassSystemProvider(): BukkitClassSystem.Provider

    /** Registers the given [registry] implemented via the given [plugin]. */
    public fun registerItemsRegistry(registry: BukkitItemsRegistry, plugin: Plugin)

    /** Returns [BukkitItemsRegistry] implementation. */
    public fun getItemsRegistry(): BukkitItemsRegistry

    /**
     * Registers [BukkitLevelSystem] implemented via the given [plugin]
     * using the default [BukkitLevelSystem.Provider] implementation.
     *
     * Default provider implementation can be used only if the given
     * [levelSystemClass] has constructor with single parameter of type [Player].
     * If you need more parameters for system initialization,
     * you should implement provider manually.
     */
    public fun registerLevelSystem(levelSystemClass: Class<out BukkitLevelSystem>, plugin: Plugin)

    /** Registers the given [provider] for [BukkitLevelSystem] implemented via the given [plugin]. */
    public fun registerLevelSystem(provider: BukkitLevelSystem.Provider, plugin: Plugin)

    /**
     * Returns top priority [BukkitLevelSystem] for the given [player].
     * It is a shorthand for `getLevelSystemProvider().getSystem(player)`.
     */
    public fun getLevelSystem(player: Player): BukkitLevelSystem

    /** Returns top priority [BukkitLevelSystem.Provider]. */
    public fun getLevelSystemProvider(): BukkitLevelSystem.Provider

    public companion object {
        /**
         * Returns [Mimic] instance.
         * This method should be called after Mimic is loaded.
         */
        @JvmStatic
        public fun getInstance(): Mimic {
            return checkNotNull(Bukkit.getServer().servicesManager.load()) {
                """
                 Your plugin was loaded earlier than Mimic.
                 Please check you have added Mimic to 'softpend' or 'depend' in the plugin.yml.
                 """.trimIndent()
            }
        }
    }
}

/** Variant of [Mimic.registerClassSystem] method with implementation class specified via type [T]. */
public inline fun <reified T : BukkitClassSystem> Mimic.registerClassSystem(plugin: Plugin) {
    registerClassSystem(T::class.java, plugin)
}

/** Variant of [Mimic.registerLevelSystem] method with implementation class specified via type [T]. */
public inline fun <reified T : BukkitLevelSystem> Mimic.registerLevelSystem(plugin: Plugin) {
    registerLevelSystem(T::class.java, plugin)
}
