// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.items

import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Vanilla items and custom items from all plugins supported by Mimic.
 * Get it via [Mimic.items][ru.endlesscode.mimic.Mimic.items].
 * To make your own plugin's items available here, implement [ItemProvider] instead.
 *
 * ### Quick start
 *
 * ```java
 * Key excalibur = Key.key("mmoitems:sword/excalibur");
 *
 * // Check the ID read from a config
 * if (!items.hasItem(excalibur)) { ... }
 *
 * // Create an item for the player
 * ItemStack sword = items.createItem(excalibur, 1, player);
 *
 * // Identify an item
 * if (items.isItem(player.getInventory().getItemInMainHand(), excalibur)) { ... }
 * ```
 *
 * ### IDs
 *
 * Items are identified by [Key], which consists of a namespace and a path:
 * - The namespace is the ID of the plugin providing the item: `minecraft`, `mmoitems`, `craftengine`.
 * - The path is the item within that plugin: `diamond`, `sword/excalibur`, `default/palm_log`.
 *
 * The namespace is required, there is no search over all plugins.
 * Note that `Key.key("excalibur")` turns into `minecraft:excalibur`,
 * so validate item IDs read from configs with [hasItem] or against [getAllItems].
 *
 * ### Ownership
 *
 * An item belongs to the plugin with the highest priority that recognizes it, and vanilla items belong to `minecraft`.
 * So a custom item is never recognized as the vanilla item it is built on:
 * a ruby built on a diamond is not `minecraft:diamond`.
 * To check the vanilla item regardless of plugins, use [ItemStack.getType].
 *
 * ### Rules
 *
 * - [createItem] must be called on the main thread, other methods may be called from any thread.
 * - Item IDs may be unknown until their plugins finish loading, and may change when a plugin reloads.
 *   Don't validate configs in `onLoad`, and don't keep created stacks across reloads.
 * - `null` or `false` means "not possible": the item doesn't exist, no plugin recognizes it, or the payload is invalid.
 *
 * @since 0.11.0
 */
public interface Items {

    /**
     * Returns IDs of all items of all providers. Use it for config validation and tab completion.
     * IDs are sorted by provider priority, then in the order the provider returns them.
     */
    public fun getAllItems(): List<Key>

    /** Returns `true` if the item with [itemId] exists. */
    public fun hasItem(itemId: Key): Boolean

    /** Returns ID of the [item] in the provider owning it, or `null` if no provider recognizes the item. */
    public fun getItemId(item: ItemStack): Key?

    /**
     * Returns `true` if the [item] has [itemId]. Same as `getItemId(item) == itemId`:
     * amount, stats, durability and lore of the stack are ignored.
     */
    public fun isItem(item: ItemStack, itemId: Key): Boolean

    /** Creates a new stack with a single item with [itemId]. */
    public fun createItem(itemId: Key): ItemStack? = createItem(itemId, amount = 1)

    /** Creates a new stack of the item with [itemId] and the given [amount]. */
    public fun createItem(itemId: Key, amount: Int): ItemStack? = createItem(itemId, amount, player = null)

    /** Creates a new stack of the item with [itemId] and the given [amount] for the [player]. */
    public fun createItem(itemId: Key, amount: Int, player: Player?): ItemStack? =
        createItem(itemId, amount, player, payload = null)

    /**
     * Creates a new stack of the item with [itemId]. The caller owns the returned stack.
     *
     * @param amount Amount of items. It is kept within `1` and the maximum stack size of the item.
     * @param player The player the item is created for, or `null` if there is no such player.
     *   Some items depend on the player, like items scaled to the player's level.
     * @param payload Additional configuration of the item in the format of its provider.
     *   A blank payload is the same as `null`.
     *   For example, `minecraft` takes item components like `[damage=10]`.
     *   Providers taking no payload ignore it. If the payload is invalid, the item is not created and a warning is logged.
     * @return the stack, or `null` if the ID is unknown or the payload is invalid.
     */
    public fun createItem(itemId: Key, amount: Int = 1, player: Player? = null, payload: String? = null): ItemStack?
}
