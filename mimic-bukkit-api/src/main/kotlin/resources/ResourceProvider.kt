// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.resources

import net.kyori.adventure.key.KeyPattern
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.MimicService

/**
 * Implement it to make your plugin's resources available via [Resources].
 * Register it in `onLoad` via [Mimic.registerResourceProvider][ru.endlesscode.mimic.Mimic.registerResourceProvider].
 * To read or change resources of players, use [Resources] instead.
 *
 * A **resource** is something a player spends and gets back, with a current amount and a maximum:
 * mana, stamina, energy, rage. Your plugin may have one resource or many.
 *
 * ### What to implement
 *
 * - Required: [allResources], [getPrimaryResource] and [getState].
 * - Optional: [setAmount]. Skip it if your plugin can't change the amount right away, it returns `false` by default.
 *
 * Mimic takes and gives resources through [getState] and [setAmount], so there is nothing else to implement.
 *
 * ### Resource IDs
 *
 * Mimic adds your provider [id] as the namespace, so return IDs without it: `mana` becomes `myplugin:mana`.
 * Don't change [id] later, it breaks consumers' configs.
 *
 * If your plugin has its own namespaced IDs, convert them to paths:
 * - Drop your plugin's own namespace: `myplugin:mana` -> `mana`.
 * - Keep other namespaces as the first path part: `myaddon:rage` -> `myaddon/rage`.
 *
 * Other rules:
 * - Allowed characters: `a-z`, `0-9`, `.`, `_`, `-` and `/`. Resources with other characters are ignored.
 * - To convert other IDs, lowercase them and replace other characters with `_`.
 *   Keep a map back to the original IDs.
 * - If two IDs become the same (`Mana` and `mana`), log a warning and keep one of them.
 *
 * ### Rules
 *
 * - Only online players are passed to the provider.
 * - Never throw. If the player's data isn't loaded yet, return `null` from reads and `false` from writes.
 * - Don't change data until the player's data is loaded, even if your plugin's API allows it.
 * - Never send messages to the player, like "not enough mana".
 * - Reads may be called from any thread. If your plugin can't read data there, return `null` instead of throwing.
 * - Writes are expected on the main thread (on Folia - on the player's thread), but Mimic doesn't enforce it.
 *
 * Mimic does the rest: selects the provider by the resource ID namespace, checks amounts,
 * never gives above [ResourceState.max] or sets below 0, and catches errors thrown by the provider.
 *
 * @since 0.10.0
 */
public interface ResourceProvider : MimicService {

    /**
     * All resources this provider knows, regardless of players.
     * The list may change at runtime, so it is read on every request.
     */
    public val allResources: Collection<ResourceDefinition>

    /**
     * Returns the resource the [player] spends on abilities by default, like mana,
     * or `null` if there is no such resource.
     */
    public fun getPrimaryResource(player: Player): ResourceDefinition?

    /**
     * Returns how much of the [resourceId] the [player] has,
     * or `null` if the player doesn't have the resource or their data isn't loaded.
     */
    public fun getState(player: Player, @KeyPattern.Value resourceId: String): ResourceState?

    /**
     * Sets the current [amount] of the [resourceId] right away, so the next [getState] returns the new value.
     * Skip this method if your plugin applies changes later.
     *
     * Mimic never passes a negative value, and passes a value above [ResourceState.max] only when taking
     * from an amount that is already above it. Round the value down if your plugin stores whole numbers.
     * If your plugin has different calls for giving and taking, use the one matching the change,
     * so its events tell them apart.
     *
     * @return `true` if the change was requested, `false` if it is unsupported for this [player] and [resourceId],
     *   or the player's data isn't loaded. Return `true` even if the plugin may cancel the change through its events.
     */
    public fun setAmount(player: Player, @KeyPattern.Value resourceId: String, amount: Double): Boolean = false

    /**
     * Resource of a [ResourceProvider]. Definitions are immutable, so they can be cached and reused.
     * @property id Resource path, like `mana` or `stamina`.
     * @property name Display name of the resource defined by the plugin.
     */
    public class ResourceDefinition(
        @param:KeyPattern.Value
        public val id: String,
        public val name: Component,
    )
}
