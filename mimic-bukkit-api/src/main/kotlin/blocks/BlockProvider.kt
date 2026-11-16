// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.blocks

import net.kyori.adventure.key.KeyPattern
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.MimicService

/**
 * Implement it to make your plugin's blocks available via [Blocks].
 * Register it in `onLoad` via [Mimic.registerBlockProvider][ru.endlesscode.mimic.Mimic.registerBlockProvider].
 * To get, set or break blocks, use [Blocks] instead.
 *
 * ### IDs
 *
 * Mimic adds your provider [id] as the namespace, so receive and return IDs without it:
 * `ruby_ore` becomes `myplugin:ruby_ore`. Don't change [id] later, it breaks consumers' configs.
 *
 * Block IDs are separate from item IDs: a block and the item placing it may have different IDs.
 *
 * If your plugin has its own namespaced IDs, convert them to paths:
 * - Drop your plugin's own namespace: `myplugin:ruby_ore` -> `ruby_ore`.
 * - Keep other namespaces as the first path part: `default:palm_log` -> `default/palm_log`.
 *
 * Other rules:
 * - Allowed characters: `a-z`, `0-9`, `.`, `_`, `-` and `/`. Blocks with other characters are ignored.
 * - To convert other IDs, lowercase them and replace other characters with `_`.
 *   Keep a map back to the original IDs.
 * - If two IDs become the same (`Ruby_Ore` and `ruby_ore`), log a warning and keep one of them.
 *
 * ### Ownership
 *
 * A block belongs to the provider with the highest priority that returns an ID for it.
 * So return an ID only for your blocks, and `null` for the rest, including the vanilla blocks your blocks are built on.
 * Vanilla blocks belong to Mimic's own `minecraft` provider, which is always asked last.
 *
 * ### Rules
 *
 * - Never throw. Return `null` from reads and `false` from changes instead.
 * - Fire no place or break events and make no protection checks, the caller is responsible for both.
 * - Block IDs may be unknown until your plugin loads its data. Return `null` or `false` for them until then.
 * - All methods are expected on the main thread (on Folia - on the thread owning the block),
 *   but Mimic doesn't enforce it.
 *
 * Mimic does the rest: selects the provider by the block ID namespace, finds the owner of a block,
 * checks that the block to set is empty, limits the amount of the created stack,
 * and catches errors thrown by the provider.
 *
 * @since 0.11.0
 */
public interface BlockProvider : MimicService {

    /**
     * IDs of all blocks this provider knows.
     * The list may change while the plugin loads or reloads its data, so it is read on every request.
     * Return a [Set] if you can, Mimic uses it to check that a block exists.
     */
    public val allBlocks: Collection<String>

    /** Returns ID of the [block], or `null` if it isn't a block of this provider. */
    public fun getBlockId(block: Block): String?

    /** Returns ID of the block placed by the [item], or `null` if the item doesn't place a block of this provider. */
    public fun getBlockIdFromItem(item: ItemStack): String?

    /**
     * Creates a new stack of the item placing the block with [blockId].
     * The caller owns the returned stack, so never return a shared one.
     * Some blocks, like fire, can't be placed by an item.
     *
     * @param amount Amount of items, at least `1`. If it is greater than the maximum stack size,
     *   Mimic lowers the amount of the returned stack.
     * @return the stack, or `null` if the block has no item or the ID is unknown.
     */
    public fun createBlockItem(@KeyPattern.Value blockId: String, amount: Int): ItemStack?

    /**
     * Sets the block with [blockId] in place of the [block]. Mimic calls it only for empty blocks.
     * Apply block physics as usual, so neighboring blocks are updated.
     *
     * @param state State of the block in the format of its provider, usually block state properties
     *   like `[facing=north]`, or `null` for the default state.
     *   If the state is invalid, log a warning and don't set the block.
     * @return `false` if the ID is unknown, the state is invalid, or the block can't be set.
     */
    public fun setBlock(block: Block, @KeyPattern.Value blockId: String, state: String?): Boolean

    /**
     * Breaks the [block] and passes the dropped items to the [dropsConsumer],
     * then drops the items it returns on the ground near the block.
     * Mimic calls it only for blocks this provider returns an ID for.
     *
     * Only items are dropped, without experience and break effects.
     *
     * @param tool The tool the block is broken with, or `null` to drop the same items as for a suitable tool
     *   without enchantments. An empty stack means the block is broken by hand.
     * @return `false` if the block can't be broken.
     */
    public fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer): Boolean
}
