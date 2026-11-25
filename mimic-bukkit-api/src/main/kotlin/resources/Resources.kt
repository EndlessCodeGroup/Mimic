// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.resources

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.LookupResult
import ru.endlesscode.mimic.LookupStatus

/**
 * Resources of players from all RPG plugins supported by Mimic: mana, stamina and similar.
 * Get it via [Mimic.resources][ru.endlesscode.mimic.Mimic.resources].
 * To make your own plugin's resources available here, implement [ResourceProvider] instead.
 *
 * A **resource** is something a player spends and gets back, with a current amount and a maximum.
 * A player may have resources from several plugins at once.
 *
 * ### Quick start
 *
 * ```java
 * // Pay a cost. Nothing is taken if the player doesn't have enough
 * Key mana = Key.key("mmocore:mana");
 * if (!resources.take(player, mana, 20.0)) {
 *     player.sendMessage("Not enough mana!");
 *     return;
 * }
 *
 * // Use the player's mana whatever plugin it comes from, server owners choose the plugin in Mimic config
 * Resource resource = resources.getPrimaryResource(player);
 * if (resource == null) return; // There is no mana on this server
 * ResourceState state = resources.getState(player, resource.key());
 * ```
 *
 * ### IDs
 *
 * Resources are identified by [Key], which consists of a namespace and a path:
 * - The namespace is the ID of the plugin providing the resource: `mmocore`, `heroes`.
 * - The path is the resource within that plugin: `mana`, `stamina`.
 *
 * The namespace is required, since the same path in different plugins means a different resource.
 * Note that `Key.key("mana")` turns into `minecraft:mana`,
 * so read resource IDs from configs with [lookupResource].
 *
 * ### Rules
 *
 * - Works only with online players.
 * - `null` or `false` means "not possible right now": the resource doesn't exist, the player doesn't have it,
 *   the plugin doesn't support this action, or the player's data isn't loaded yet.
 * - Don't cache results. Amounts change all the time because of regeneration,
 *   and a class change may replace them.
 * - Plugins storing whole numbers round amounts down. So [take] may take a bit more, and [give] a bit less.
 *   An amount smaller than the plugin can store, like a millionth of health, isn't taken at all.
 * - Mimic never sends messages to the player.
 * - Read from any thread. Change resources only on the main thread (on Folia - on the player's thread).
 *
 * @since 0.10.0
 */
public interface Resources {

    /**
     * Returns all resources of all providers, regardless of players. Use it for discovery and config validation.
     * Resources are sorted by provider priority, then in the order the provider returns them.
     */
    public fun getAllResources(): List<Resource>

    /**
     * Looks up the resource ID by a [value] from a config. The result has only IDs of existing resources.
     * Use it instead of `Key.key(String)`, which turns `mana` into `minecraft:mana`.
     *
     * The lookup is lenient:
     * - Letter case doesn't matter, and characters not allowed in IDs are replaced with `_`: `MMOCore:Mana` is `mmocore:mana`.
     * - The namespace may be omitted if only one plugin has a resource with this name: `stellium` is `mmocore:stellium`.
     *   If several plugins have it, the plugin set in the `resource-provider` option of Mimic config is used.
     *   If the option isn't set, or that plugin doesn't have the resource, the result is [LookupStatus.AMBIGUOUS].
     *
     * May be called from any thread.
     *
     * @since 0.12.0
     */
    public fun lookupResource(value: String): LookupResult

    /**
     * Returns the resource the [player] spends on abilities by default, like mana,
     * or `null` if there is no such resource.
     *
     * Server owners choose the plugin for it with the `resource-provider` option in Mimic config.
     * If the option isn't set, or the plugin isn't installed, Mimic asks plugins one by one,
     * from the highest priority, and returns the first primary resource it gets.
     * Vanilla resources, like `minecraft:food`, are never primary.
     */
    public fun getPrimaryResource(player: Player): Resource?

    /**
     * Returns how much of the [resourceId] the [player] has,
     * or `null` if the player doesn't have the resource or their data isn't loaded.
     */
    public fun getState(player: Player, resourceId: Key): ResourceState?

    /**
     * Returns `true` if the [player] has at least [amount] of the [resourceId].
     *
     * @param amount Non-negative amount of the resource.
     * @throws IllegalArgumentException if [amount] is negative or not finite.
     */
    public fun has(player: Player, resourceId: Key, amount: Double): Boolean

    /**
     * Takes [amount] of the [resourceId], or nothing if the [player] has less.
     * The plugin may take a different amount by its own rules, for example with a discount.
     *
     * @param amount Non-negative amount of the resource.
     * @return `true` if the resource was taken, `false` if the player doesn't have enough, another plugin cancelled
     *   the change, it is unsupported, or the player's data isn't loaded.
     * @throws IllegalArgumentException if [amount] is negative or not finite.
     */
    public fun take(player: Player, resourceId: Key, amount: Double): Boolean

    /**
     * Gives [amount] of the [resourceId], but not above [ResourceState.max].
     *
     * @param amount Non-negative amount of the resource.
     * @return `true` if the change was requested, `false` if it is unsupported or the player's data isn't loaded.
     *   `true` doesn't guarantee the change: another plugin may cancel it, and Mimic can't detect that.
     * @throws IllegalArgumentException if [amount] is negative or not finite.
     */
    public fun give(player: Player, resourceId: Key, amount: Double): Boolean

    /**
     * Sets the current [amount] of the [resourceId]. The amount is kept between 0 and [ResourceState.max].
     *
     * @return `true` if the change was requested, `false` if it is unsupported or the player's data isn't loaded.
     *   `true` doesn't guarantee the change: another plugin may cancel it, and Mimic can't detect that.
     * @throws IllegalArgumentException if [amount] is not finite.
     */
    public fun set(player: Player, resourceId: Key, amount: Double): Boolean
}

/**
 * Resource of a plugin. Get all resources via [Resources.getAllResources].
 * Resources are equal if their keys are equal.
 * @since 0.10.0
 */
public interface Resource : Keyed {

    /** Full resource ID, where namespace is the provider ID: `mmocore:mana`. */
    override fun key(): Key

    /** Display name of the resource defined by the plugin. */
    public fun name(): Component
}
