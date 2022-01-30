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
     * Registers the given [provider] for [BukkitClassSystem].
     *
     * @param provider The provider used to get class system.
     * @param apiLevel Minimal required API level for this class system implementation:
     *                 - if required API level is higher than installed Mimic, provider will not be registered,
     *                 - if required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used on compile time.
     * @param plugin The plugin implementing this class system.
     * @return registered class system or `null` if it was not registered.
     */
    public fun registerClassSystem(
        provider: BukkitClassSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
    ): BukkitClassSystem.Provider?

    /**
     * Returns top priority [BukkitClassSystem] for the given [player].
     * It is a shorthand for `getClassSystemProvider().getSystem(player)`.
     */
    public fun getClassSystem(player: Player): BukkitClassSystem

    /** Returns top priority [BukkitClassSystem.Provider]. */
    public fun getClassSystemProvider(): BukkitClassSystem.Provider

    /**
     * Registers the given [registry].
     *
     * @param registry The [BukkitItemsRegistry] implementation
     * @param apiLevel Minimal required API level for this item registry implementation:
     *                 - if required API level is higher than installed Mimic, provider will not be registered,
     *                 - if required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used on compile time.
     * @param plugin The plugin implementing this item registry.
     * @return registered registry or `null` if it was not registered.
     */
    public fun registerItemsRegistry(
        registry: BukkitItemsRegistry,
        apiLevel: Int,
        plugin: Plugin,
    ): BukkitItemsRegistry?

    /** Returns [BukkitItemsRegistry] implementation. */
    public fun getItemsRegistry(): BukkitItemsRegistry

    /**
     * Registers the given [provider] for [BukkitLevelSystem].
     *
     * @param provider The provider used to get level system.
     * @param apiLevel Minimal required API level for this level system implementation:
     *                 - if required API level is higher than installed Mimic, provider will not be registered,
     *                 - if required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used on compile time.
     * @param plugin The plugin implementing this level system.
     * @return registered level system or `null` if it was not registered.
     */
    public fun registerLevelSystem(
        provider: BukkitLevelSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
    ): BukkitLevelSystem.Provider?

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
