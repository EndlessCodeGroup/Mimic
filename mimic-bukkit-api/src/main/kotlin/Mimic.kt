// Copyright (c) 2022 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // LevelSystem, ClassSystem and ItemsRegistry APIs are deprecated but still supported

package ru.endlesscode.mimic

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import org.jetbrains.annotations.ApiStatus
import ru.endlesscode.mimic.blocks.BlockProvider
import ru.endlesscode.mimic.blocks.Blocks
import ru.endlesscode.mimic.bukkit.load
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.classes.ClassProvider
import ru.endlesscode.mimic.classes.Classes
import ru.endlesscode.mimic.inventory.BukkitPlayerInventory
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.items.ItemProvider
import ru.endlesscode.mimic.items.Items
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.level.LevelProvider
import ru.endlesscode.mimic.level.Levels
import ru.endlesscode.mimic.resources.ResourceProvider
import ru.endlesscode.mimic.resources.Resources

/** Mimic provides access to abstraction APIs. */
public interface Mimic {

    // region Class System
    /**
     * Registers the given [provider] for [BukkitClassSystem] with normal priority.
     *
     * @param provider The provider used to get class system.
     * @param apiLevel Minimal required API level for this class system implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this class system.
     * @return registered class system or `null` if it was not registered.
     */
    @Deprecated("Use registerClassProvider instead.")
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
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this class system.
     * @param priority Default priority. Service with higher priority will be used if user have not configured
     *                 preferred service in config.
     * @return registered class system or `null` if it was not registered.
     */
    @Deprecated("Use registerClassProvider instead.")
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
    @Deprecated("Use classes.getPrimaryClass(player) or classes.getClasses(player) instead.")
    public fun getClassSystem(player: Player): BukkitClassSystem = getClassSystemProvider().getSystem(player)

    /** Returns top priority [BukkitClassSystem.Provider]. */
    @Deprecated("Use classes instead.")
    public fun getClassSystemProvider(): BukkitClassSystem.Provider

    /** Returns map containing all known [BukkitClassSystem.Provider]s, where key is a provider ID. */
    @Deprecated("Use classes.getAllSlots() and classes.getAllClasses() instead.")
    public fun getAllClassSystemProviders(): Map<String, BukkitClassSystem.Provider>
    // endregion

    // region Inventory Provider
    /**
     * Registers the given [provider] for [BukkitPlayerInventory] with normal priority.
     *
     * @param provider The provider used to get player inventory.
     * @param apiLevel Minimal required API level for this level system implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this player inventory.
     * @return registered level system or `null` if it was not registered.
     * @since 0.8.0
     */
    @ExperimentalMimicApi
    @ApiStatus.Experimental
    public fun registerPlayerInventoryProvider(
        provider: BukkitPlayerInventory.Provider,
        apiLevel: Int,
        plugin: Plugin,
    ): BukkitPlayerInventory.Provider? =
        registerPlayerInventoryProvider(provider, apiLevel, plugin, ServicePriority.Normal)

    /**
     * Registers the given [provider] for [BukkitPlayerInventory].
     *
     * @param provider The provider used to get player inventory.
     * @param apiLevel Minimal required API level for this level system implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this player inventory.
     * @param priority Default priority. Service with higher priority will be used if user have not configured
     *                 preferred service in config.
     * @return registered level system or `null` if it was not registered.
     * @since 0.8.0
     */
    @ExperimentalMimicApi
    @ApiStatus.Experimental
    public fun registerPlayerInventoryProvider(
        provider: BukkitPlayerInventory.Provider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitPlayerInventory.Provider?

    /**
     * Returns top priority [BukkitPlayerInventory] for the given [player].
     * It is a shorthand for `getPlayerInventoryProvider().getSystem(player)`.
     * @since 0.8.0
     */
    @ExperimentalMimicApi
    @ApiStatus.Experimental
    public fun getPlayerInventory(player: Player): BukkitPlayerInventory =
        getPlayerInventoryProvider().getSystem(player)

    /**
     * Returns top priority [BukkitPlayerInventory.Provider].
     * @since 0.8.0
     */
    @ExperimentalMimicApi
    @ApiStatus.Experimental
    public fun getPlayerInventoryProvider(): BukkitPlayerInventory.Provider

    /**
     * Returns map containing all known [BukkitPlayerInventory.Provider]s,
     * where key is a provider ID.
     * @since 0.8.0
     */
    @ExperimentalMimicApi
    @ApiStatus.Experimental
    public fun getAllPlayerInventoryProviders(): Map<String, BukkitPlayerInventory.Provider>
    // endregion

    // region Items Registry
    /**
     * Registers the given [registry] with normal priority.
     *
     * @param registry The [BukkitItemsRegistry] implementation
     * @param apiLevel Minimal required API level for this item registry implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this item registry.
     * @return registered registry or `null` if it was not registered.
     */
    @Deprecated("Use registerItemProvider instead.")
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
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this item registry.
     * @param priority Default priority. Service with higher priority will be used if user have not configured
     *                 preferred service in config.
     * @return registered registry or `null` if it was not registered.
     */
    @Deprecated("Use registerItemProvider instead.")
    public fun registerItemsRegistry(
        registry: BukkitItemsRegistry,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BukkitItemsRegistry?

    /** Returns [BukkitItemsRegistry] implementation. */
    @Deprecated("Use items instead.")
    public val itemsRegistry: BukkitItemsRegistry

    /** Returns map containing all known [BukkitItemsRegistry], where key is an item registry ID. */
    @Deprecated("Use items.getAllItems() instead.")
    public fun getAllItemsRegistries(): Map<String, BukkitItemsRegistry>
    // endregion

    // region Items
    /**
     * Registers the given [provider] of items with normal priority.
     *
     * @param provider The [ItemProvider] implementation.
     * @param apiLevel Minimal required API level for this item provider implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this item provider.
     * @return registered provider or `null` if it was not registered.
     * @since 0.11.0
     */
    public fun registerItemProvider(
        provider: ItemProvider,
        apiLevel: Int,
        plugin: Plugin,
    ): ItemProvider? = registerItemProvider(provider, apiLevel, plugin, ServicePriority.Normal)

    /**
     * Registers the given [provider] of items.
     *
     * @param provider The [ItemProvider] implementation.
     * @param apiLevel Minimal required API level for this item provider implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this item provider.
     * @param priority Default priority. An item recognized by several providers belongs to the provider
     *                 with the highest priority.
     * @return registered provider or `null` if it was not registered.
     * @since 0.11.0
     */
    public fun registerItemProvider(
        provider: ItemProvider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): ItemProvider?

    /**
     * Vanilla and custom items from all plugins: create them by ID, identify stacks, list and validate IDs.
     * See [Items] for details and examples.
     * To make your plugin's items available here, use [registerItemProvider].
     * @since 0.11.0
     */
    public val items: Items
    // endregion

    // region Level System
    /**
     * Registers the given [provider] for [BukkitLevelSystem] with normal priority.
     *
     * @param provider The provider used to get level system.
     * @param apiLevel Minimal required API level for this level system implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this level system.
     * @return registered level system or `null` if it was not registered.
     */
    @Deprecated("Use registerLevelProvider instead.")
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
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this level system.
     * @param priority Default priority. Service with higher priority will be used if user have not configured
     *                 preferred service in config.
     * @return registered level system or `null` if it was not registered.
     */
    @Deprecated("Use registerLevelProvider instead.")
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
    @Deprecated("Use levels.getPrimaryTrack(player) and levels.getProgress(player, trackId) instead.")
    public fun getLevelSystem(player: Player): BukkitLevelSystem = getLevelSystemProvider().getSystem(player)

    /** Returns top priority [BukkitLevelSystem.Provider]. */
    @Deprecated("Use levels instead.")
    public fun getLevelSystemProvider(): BukkitLevelSystem.Provider

    /** Returns map containing all known [BukkitLevelSystem.Provider]s, where key is a provider ID. */
    @Deprecated("Use levels.getAllTracks() instead.")
    public fun getAllLevelSystemProviders(): Map<String, BukkitLevelSystem.Provider>
    // endregion

    // region Levels
    /**
     * Registers the given [provider] of level tracks with normal priority.
     *
     * @param provider The [LevelProvider] implementation.
     * @param apiLevel Minimal required API level for this level provider implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this level provider.
     * @return registered provider or `null` if it was not registered.
     * @since 0.10.0
     */
    public fun registerLevelProvider(
        provider: LevelProvider,
        apiLevel: Int,
        plugin: Plugin,
    ): LevelProvider? = registerLevelProvider(provider, apiLevel, plugin, ServicePriority.Normal)

    /**
     * Registers the given [provider] of level tracks.
     *
     * @param provider The [LevelProvider] implementation.
     * @param apiLevel Minimal required API level for this level provider implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this level provider.
     * @param priority Default priority. If user have not configured `level-provider` option in config,
     *                 the primary track is taken from the first provider in order of priority that has one.
     * @return registered provider or `null` if it was not registered.
     * @since 0.10.0
     */
    public fun registerLevelProvider(
        provider: LevelProvider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): LevelProvider?

    /**
     * Levels of players from all RPG plugins: read them, check level requirements, give or take experience.
     * See [Levels] for details and examples.
     * To make your plugin's levels available here, use [registerLevelProvider].
     * @since 0.10.0
     */
    public val levels: Levels
    // endregion

    // region Classes
    /**
     * Registers the given [provider] of classes with normal priority.
     *
     * @param provider The [ClassProvider] implementation.
     * @param apiLevel Minimal required API level for this class provider implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this class provider.
     * @return registered provider or `null` if it was not registered.
     * @since 0.10.0
     */
    public fun registerClassProvider(
        provider: ClassProvider,
        apiLevel: Int,
        plugin: Plugin,
    ): ClassProvider? = registerClassProvider(provider, apiLevel, plugin, ServicePriority.Normal)

    /**
     * Registers the given [provider] of classes.
     *
     * @param provider The [ClassProvider] implementation.
     * @param apiLevel Minimal required API level for this class provider implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this class provider.
     * @param priority Default priority. If user have not configured `class-provider` option in config,
     *                 the primary class is taken from the first provider in order of priority that has one.
     * @return registered provider or `null` if it was not registered.
     * @since 0.10.0
     */
    public fun registerClassProvider(
        provider: ClassProvider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): ClassProvider?

    /**
     * Classes of players from all RPG plugins: read them, check class requirements, change classes.
     * See [Classes] for details and examples.
     * To make your plugin's classes available here, use [registerClassProvider].
     * @since 0.10.0
     */
    public val classes: Classes
    // endregion

    // region Resources
    /**
     * Registers the given [provider] of resources with normal priority.
     *
     * @param provider The [ResourceProvider] implementation.
     * @param apiLevel Minimal required API level for this resource provider implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this resource provider.
     * @return registered provider or `null` if it was not registered.
     * @since 0.10.0
     */
    public fun registerResourceProvider(
        provider: ResourceProvider,
        apiLevel: Int,
        plugin: Plugin,
    ): ResourceProvider? = registerResourceProvider(provider, apiLevel, plugin, ServicePriority.Normal)

    /**
     * Registers the given [provider] of resources.
     *
     * @param provider The [ResourceProvider] implementation.
     * @param apiLevel Minimal required API level for this resource provider implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this resource provider.
     * @param priority Default priority. If user have not configured `resource-provider` option in config,
     *                 the primary resource is taken from the first provider in order of priority that has one.
     * @return registered provider or `null` if it was not registered.
     * @since 0.10.0
     */
    public fun registerResourceProvider(
        provider: ResourceProvider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): ResourceProvider?

    /**
     * Resources of players from all RPG plugins, like mana and stamina: read them, take costs, give them back.
     * See [Resources] for details and examples.
     * To make your plugin's resources available here, use [registerResourceProvider].
     * @since 0.10.0
     */
    public val resources: Resources
    // endregion

    // region Blocks
    /**
     * Registers the given [provider] of blocks with normal priority.
     *
     * @param provider The [BlockProvider] implementation.
     * @param apiLevel Minimal required API level for this block provider implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this block provider.
     * @return registered provider or `null` if it was not registered.
     * @since 0.11.0
     */
    public fun registerBlockProvider(
        provider: BlockProvider,
        apiLevel: Int,
        plugin: Plugin,
    ): BlockProvider? = registerBlockProvider(provider, apiLevel, plugin, ServicePriority.Normal)

    /**
     * Registers the given [provider] of blocks.
     *
     * @param provider The [BlockProvider] implementation.
     * @param apiLevel Minimal required API level for this block provider implementation:
     *                 - if the required API level is higher than installed Mimic, provider will not be registered,
     *                 - if the required API level is lower - will be enabled compatibility mode.
     *                 Specify `MimicApiLevel.CURRENT` to use API level of Mimic dependency used at compile time.
     * @param plugin The plugin implementing this block provider.
     * @param priority Default priority. A block recognized by several providers belongs to the provider
     *                 with the highest priority.
     * @return registered provider or `null` if it was not registered.
     * @since 0.11.0
     */
    public fun registerBlockProvider(
        provider: BlockProvider,
        apiLevel: Int,
        plugin: Plugin,
        priority: ServicePriority,
    ): BlockProvider?

    /**
     * Vanilla and custom blocks from all plugins: identify placed blocks, set and break them, get items placing them.
     * See [Blocks] for details and examples.
     * To make your plugin's blocks available here, use [registerBlockProvider].
     * @since 0.11.0
     */
    public val blocks: Blocks
    // endregion

    public companion object {
        /**
         * Returns [Mimic] instance.
         * This method should be called after Mimic is loaded.
         */
        @JvmStatic
        public fun getInstance(): Mimic {
            return checkNotNull(Bukkit.getServer().servicesManager.load()) {
                """
                Your plugin was loaded before Mimic.
                Please check you have added Mimic to 'softpend' or 'depend' in the plugin.yml.
                """.trimIndent()
            }
        }
    }
}
