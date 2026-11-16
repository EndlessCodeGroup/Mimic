// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.blocks

import net.kyori.adventure.key.Key
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

/**
 * Vanilla blocks and custom blocks from all plugins supported by Mimic.
 * Get it via [Mimic.blocks][ru.endlesscode.mimic.Mimic.blocks].
 * To make your own plugin's blocks available here, implement [BlockProvider] instead.
 *
 * ### Quick start
 *
 * ```java
 * Key palmLog = Key.key("craftengine:default/palm_log");
 *
 * // Check the ID read from a config
 * if (!blocks.hasBlock(palmLog)) { ... }
 *
 * // Identify a placed block
 * if (blocks.isBlock(block, palmLog)) { ... }
 *
 * // Set a block in place of an empty one
 * blocks.setBlock(location.getBlock(), palmLog, "[axis=x]");
 *
 * // Break a block with a pickaxe, dropping items on the ground
 * blocks.breakBlock(block, pickaxe);
 * ```
 *
 * ### IDs
 *
 * Blocks are identified by [Key], which consists of a namespace and a path:
 * - The namespace is the ID of the plugin providing the block: `minecraft`, `craftengine`.
 * - The path is the block within that plugin: `stone`, `default/palm_log`.
 *
 * The namespace is required, there is no search over all plugins.
 * Note that `Key.key("palm_log")` turns into `minecraft:palm_log`,
 * so validate block IDs read from configs with [hasBlock] or against [getAllBlocks].
 * Block IDs are separate from item IDs: a block and the item placing it may have different IDs.
 *
 * ### Ownership
 *
 * A block belongs to the plugin with the highest priority that recognizes it, and vanilla blocks belong to `minecraft`.
 * So a custom block is never recognized as the vanilla block it is built on:
 * a custom block built on a note block is not `minecraft:note_block`.
 * To check the vanilla block regardless of plugins, use [Block.getType].
 *
 * ### Rules
 *
 * - Call all methods on the main thread (on Folia - on the thread owning the block), since they touch the world.
 * - No place or break events are fired and no protection checks are made, the caller is responsible for both.
 * - Block IDs may be unknown until their plugins finish loading, and may change when a plugin reloads.
 *   Don't validate configs in `onLoad`, and don't cache results.
 * - `null` or `false` means "not possible": the block doesn't exist, no plugin recognizes it, or the change failed.
 *
 * @since 0.11.0
 */
public interface Blocks {

    /**
     * Returns IDs of all blocks of all providers. Use it for config validation and tab completion.
     * IDs are sorted by provider priority, then in the order the provider returns them.
     */
    public fun getAllBlocks(): List<Key>

    /** Returns `true` if the block with [blockId] exists. */
    public fun hasBlock(blockId: Key): Boolean

    /** Returns ID of the [block] in the provider owning it, or `null` if no provider recognizes the block. */
    public fun getBlockId(block: Block): Key?

    /** Returns `true` if the [block] has [blockId]. Same as `getBlockId(block) == blockId`. */
    public fun isBlock(block: Block, blockId: Key): Boolean

    /**
     * Returns ID of the block placed by the [item] in the provider owning the item,
     * or `null` if the item doesn't place a block.
     */
    public fun getBlockIdFromItem(item: ItemStack): Key?

    /** Creates a new stack with a single item placing the block with [blockId]. */
    public fun createBlockItem(blockId: Key): ItemStack? = createBlockItem(blockId, amount = 1)

    /**
     * Creates a new stack of the item placing the block with [blockId]. The caller owns the returned stack.
     * Some blocks, like fire, can't be placed by an item.
     *
     * @param amount Amount of items. It is kept within `1` and the maximum stack size of the item.
     * @return the stack, or `null` if the block has no item or the ID is unknown.
     */
    public fun createBlockItem(blockId: Key, amount: Int): ItemStack?

    /** Sets the block with [blockId] in its default state in place of the empty [block]. */
    public fun setBlock(block: Block, blockId: Key): Boolean = setBlock(block, blockId, state = null)

    /**
     * Sets the block with [blockId] in place of the empty [block].
     * To replace a block, break it first, so its plugin can clean up.
     * Block physics is applied as usual, so neighboring blocks are updated.
     *
     * @param state State of the block in the format of its provider, usually block state properties
     *   like `[facing=north]`, or `null` for the default state.
     *   If the state is invalid, the block is not set and a warning is logged.
     * @return `false` if the [block] is not empty, the ID is unknown, the state is invalid, or the block can't be set.
     */
    public fun setBlock(block: Block, blockId: Key, state: String?): Boolean

    /** Breaks the [block] and drops the same items on the ground as for a suitable tool without enchantments. */
    public fun breakBlock(block: Block): Boolean = breakBlock(block, tool = null)

    /** Breaks the [block] with the [tool] and drops the items on the ground. */
    public fun breakBlock(block: Block, tool: ItemStack?): Boolean = breakBlock(block, tool, DropsConsumer.drop())

    /**
     * Breaks the [block] via the provider owning it and passes the dropped items to the [dropsConsumer].
     * Only items are dropped, without experience and break effects.
     *
     * @param tool The tool the block is broken with, or `null` to drop the same items as for a suitable tool
     *   without enchantments. Pass an empty stack to break the block by hand.
     * @param dropsConsumer Use [DropsConsumer.drop] to drop all items on the ground,
     *   or [DropsConsumer.disappear] to drop nothing.
     * @return `false` if the block can't be broken or no provider recognizes it.
     */
    public fun breakBlock(block: Block, tool: ItemStack? = null, dropsConsumer: DropsConsumer): Boolean
}
