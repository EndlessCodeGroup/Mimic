package ru.endlesscode.mimic

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import ru.endlesscode.mimic.bukkit.load
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.inventory.BukkitPlayerInventory
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.level.BukkitLevelSystem

/** Mimic provides access to abstraction APIs. */
public interface Mimic {

    /**
     * Registers the given [provider] for [BukkitClassSystem] with normal priority.
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
    ): BukkitClassSystem.Provider? = registerClassSystem(provider, apiLevel, plugin, ServicePriority.Normal)

    /**
     * Registers the given [provider] for [BukkitClassSystem].
     *
     * @param provider The provider used to get class system.
     * @param apiLevel Minimal required API level for this class system implementation:
     *                 - if required API level is higher than installed Mimic, provider will not be registered,
     *                 - if required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used on compile time.
     * @param plugin The plugin implementing this class system.
     * @param priority Default priority. User may override it though config.
     * @return registered class system or `null` if it was not registered.
     */
    public fun registerClassSystem(
        provider: BukkitClassSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitClassSystem.Provider?

    /**
     * Returns top priority [BukkitClassSystem] for the given [player].
     * It is a shorthand for `getClassSystemProvider().getSystem(player)`.
     */
    public fun getClassSystem(player: Player): BukkitClassSystem

    /** Returns top priority [BukkitClassSystem.Provider]. */
    public fun getClassSystemProvider(): BukkitClassSystem.Provider

    /** Returns map containing all known [BukkitClassSystem.Provider]s, where key is a provider ID. */
    public fun getAllClassSystemProviders(): Map<String, BukkitClassSystem.Provider>

    /**
     * Registers the given [provider] for [BukkitPlayerInventory] with normal priority.
     *
     * @param provider The provider used to get player inventory.
     * @param apiLevel Minimal required API level for this level system implementation:
     *                 - if required API level is higher than installed Mimic, provider will not be registered,
     *                 - if required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used on compile time.
     * @param plugin The plugin implementing this player inventory.
     * @return registered level system or `null` if it was not registered.
     */
    @ExperimentalMimicApi
    public fun registerPlayerInventoryProvider(
        provider: BukkitPlayerInventory.Provider,
        apiLevel: Int,
        plugin: Plugin,
    ): BukkitPlayerInventory.Provider? = registerPlayerInventoryProvider(provider, apiLevel, plugin, ServicePriority.Normal)

    /**
     * Registers the given [provider] for [BukkitPlayerInventory].
     *
     * @param provider The provider used to get player inventory.
     * @param apiLevel Minimal required API level for this level system implementation:
     *                 - if required API level is higher than installed Mimic, provider will not be registered,
     *                 - if required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used on compile time.
     * @param plugin The plugin implementing this player inventory.
     * @param priority Default priority.
     * @return registered level system or `null` if it was not registered.
     */
    @ExperimentalMimicApi
    public fun registerPlayerInventoryProvider(
        provider: BukkitPlayerInventory.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitPlayerInventory.Provider?

    /**
     * Returns top priority [BukkitPlayerInventory] for the given [player].
     * It is a shorthand for `getPlayerInventoryProvider().getSystem(player)`.
     */
    @ExperimentalMimicApi
    public fun getPlayerInventory(player: Player): BukkitPlayerInventory =
        getPlayerInventoryProvider().getSystem(player)

    /** Returns top priority [BukkitPlayerInventory.Provider]. */
    @ExperimentalMimicApi
    public fun getPlayerInventoryProvider(): BukkitPlayerInventory.Provider

    /** Returns map containing all known [BukkitPlayerInventory.Provider]s, where key is a provider ID. */
    @ExperimentalMimicApi
    public fun getAllPlayerInventoryProviders(): Map<String, BukkitPlayerInventory.Provider>

    /**
     * Registers the given [registry] with normal priority.
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
    ): BukkitItemsRegistry? = registerItemsRegistry(registry, apiLevel, plugin, ServicePriority.Normal)

    /**
     * Registers the given [registry].
     *
     * @param registry The [BukkitItemsRegistry] implementation
     * @param apiLevel Minimal required API level for this item registry implementation:
     *                 - if required API level is higher than installed Mimic, provider will not be registered,
     *                 - if required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used on compile time.
     * @param plugin The plugin implementing this item registry.
     * @param priority Default priority. User may override it though config.
     * @return registered registry or `null` if it was not registered.
     */
    public fun registerItemsRegistry(
        registry: BukkitItemsRegistry,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitItemsRegistry?

    /** Returns [BukkitItemsRegistry] implementation. */
    public fun getItemsRegistry(): BukkitItemsRegistry

    /** Returns map containing all known [BukkitItemsRegistry], where key is an item registry ID. */
    public fun getAllItemsRegistries(): Map<String, BukkitItemsRegistry>

    /**
     * Registers the given [provider] for [BukkitLevelSystem] with normal priority.
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
    ): BukkitLevelSystem.Provider? = registerLevelSystem(provider, apiLevel, plugin, ServicePriority.Normal)

    /**
     * Registers the given [provider] for [BukkitLevelSystem].
     *
     * @param provider The provider used to get level system.
     * @param apiLevel Minimal required API level for this level system implementation:
     *                 - if required API level is higher than installed Mimic, provider will not be registered,
     *                 - if required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used on compile time.
     * @param plugin The plugin implementing this level system.
     * @param priority Default priority. User may override it though config.
     * @return registered level system or `null` if it was not registered.
     */
    public fun registerLevelSystem(
        provider: BukkitLevelSystem.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitLevelSystem.Provider?

    /**
     * Returns top priority [BukkitLevelSystem] for the given [player].
     * It is a shorthand for `getLevelSystemProvider().getSystem(player)`.
     */
    public fun getLevelSystem(player: Player): BukkitLevelSystem

    /** Returns top priority [BukkitLevelSystem.Provider]. */
    public fun getLevelSystemProvider(): BukkitLevelSystem.Provider

    /** Returns map containing all known [BukkitLevelSystem.Provider]s, where key is a provider ID. */
    public fun getAllLevelSystemProviders(): Map<String, BukkitLevelSystem.Provider>

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
