// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.items

import net.kyori.adventure.key.KeyPattern
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.MimicService

/**
 * Implement it to make your plugin's items available via [Items].
 * Register it in `onLoad` via [Mimic.registerItemProvider][ru.endlesscode.mimic.Mimic.registerItemProvider].
 * To create or identify items, use [Items] instead.
 *
 * ### IDs
 *
 * Mimic adds your provider [id] as the namespace, so receive and return IDs without it:
 * `ruby_sword` becomes `myplugin:ruby_sword`. Don't change [id] later, it breaks consumers' configs.
 *
 * If your plugin has its own namespaced IDs, convert them to paths:
 * - Drop your plugin's own namespace: `myplugin:ruby_sword` -> `ruby_sword`.
 * - Keep other namespaces as the first path part: `default:palm_log` -> `default/palm_log`.
 *
 * Other rules:
 * - Allowed characters: `a-z`, `0-9`, `.`, `_`, `-` and `/`. Items with other characters are ignored.
 * - To convert other IDs, lowercase them and replace other characters with `_`.
 *   Keep a map back to the original IDs.
 * - If two IDs become the same (`Ruby_Sword` and `ruby_sword`), log a warning and keep one of them.
 *
 * ### Ownership
 *
 * An item belongs to the provider with the highest priority that returns an ID for it.
 * So return an ID only for your items, and `null` for the rest, including the vanilla items your items are built on.
 * Vanilla items belong to Mimic's own `minecraft` provider, which is always asked last.
 *
 * ### Rules
 *
 * - Never throw. Return `null` instead.
 * - Item IDs may be unknown until your plugin loads its data. Return `null` for them until then.
 * - [allItems] and [getItemId] may be called from any thread, [createItem] is called on the main thread.
 * - Document the format of the payload your provider takes, or that it takes none.
 *
 * Mimic does the rest: selects the provider by the item ID namespace, finds the owner of an item,
 * compares items by ID, limits the amount of the created stack, and catches errors thrown by the provider.
 *
 * @since 0.11.0
 */
public interface ItemProvider : MimicService {

    /**
     * IDs of all items this provider knows.
     * The list may change while the plugin loads or reloads its data, so it is read on every request.
     * Return a [Set] if you can, Mimic uses it to check that an item exists.
     */
    public val allItems: Collection<String>

    /**
     * Returns ID of the [item], or `null` if it isn't an item of this provider.
     * Amount, stats, durability and lore of the stack don't affect the ID.
     */
    public fun getItemId(item: ItemStack): String?

    /**
     * Creates a new stack of the item with [itemId].
     * The caller owns the returned stack, so never return a shared one.
     *
     * @param amount Amount of items, at least `1`. If it is greater than the maximum stack size,
     *   Mimic lowers the amount of the returned stack.
     * @param player The player the item is created for, or `null` if there is no such player.
     *   Use it if the item depends on the player, like items scaled to the player's level.
     * @param payload Additional configuration of the item in the format of this provider, or `null`.
     *   If the provider takes no payload, ignore it. If the payload can't be parsed, return `null`:
     *   Mimic logs a warning with the item ID and the payload.
     * @return the stack, or `null` if the ID is unknown or the payload is invalid.
     */
    public fun createItem(
        @KeyPattern.Value itemId: String,
        amount: Int,
        player: Player?,
        payload: String?,
    ): ItemStack?
}
