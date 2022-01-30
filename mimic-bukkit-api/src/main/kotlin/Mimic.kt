package ru.endlesscode.mimic

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.bukkit.load
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.level.BukkitLevelSystem
import java.util.function.Function

/** Mimic provides access to abstraction APIs. */
public interface Mimic {

    /**
     * Registers [BukkitClassSystem] implemented via the given [plugin],
     * uses the given [provider] function to get system instance.
     */
    public fun registerClassSystem(provider: Function<Player, out BukkitClassSystem>, plugin: Plugin)

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
     * Registers [BukkitLevelSystem] implemented via the given [plugin],
     * uses the given [provider] function to get system instance.
     */
    public fun registerLevelSystem(provider: Function<Player, out BukkitLevelSystem>, plugin: Plugin)

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
