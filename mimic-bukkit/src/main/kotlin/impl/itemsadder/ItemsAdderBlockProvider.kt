// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.itemsadder

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.blocks.BlockProvider
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.NormalizedIds

/**
 * Block provider for ItemsAdder custom blocks.
 * The default `itemsadder` namespace is dropped from block ID, other namespaces become the first path segment:
 * `itemsadder:ruby_ore` -> `ruby_ore`, `my_blocks:ruby_ore` -> `my_blocks/ruby_ore`.
 * Then the ID is converted with [NormalizedIds], as item IDs.
 * A block has the same ID as the item placing it.
 *
 * ItemsAdder blocks have no states, so [setBlock] with a state sets nothing.
 * Furniture is not a block.
 */
internal class ItemsAdderBlockProvider(
    private val itemsAdder: ItemsAdderWrapper = ItemsAdderWrapper(),
) : BlockProvider {

    private val ids = NormalizedIds(
        tag = "ItemsAdder",
        exists = { itemsAdder.hasBlock(itemsAdderId(it)) },
        nativeIds = { itemsAdder.blockIds.map(String::toMimicId) },
    )

    override val id: String = ID
    override val isEnabled: Boolean get() = itemsAdder.isEnabled

    override val allBlocks: Collection<String> get() = ids.all

    override fun hasBlock(blockId: String): Boolean = ids.nativeOf(blockId) != null

    override fun getBlockId(block: Block): String? = itemsAdder.getCustomBlock(block)?.namespacedID?.let(::mimicIdOf)

    override fun getBlockIdFromItem(item: ItemStack): String? =
        itemsAdder.getCustomBlock(item)?.namespacedID?.let(::mimicIdOf)

    override fun createBlockItem(blockId: String, amount: Int): ItemStack? {
        val customBlock = itemsAdderIdOf(blockId)?.let(itemsAdder::getCustomBlock) ?: return null
        return customBlock.itemStack.also { it.amount = amount }
    }

    override fun setBlock(block: Block, blockId: String, state: String?): Boolean {
        val itemsAdderId = itemsAdderIdOf(blockId) ?: return false
        if (state != null) {
            Log.w("[$TAG] Invalid state for block $blockId: $state. ItemsAdder blocks have no states.")
            return false
        }
        return itemsAdder.placeBlock(itemsAdderId, block.location)
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer): Boolean {
        val customBlock = itemsAdder.getCustomBlock(block) ?: return false

        // The block itself is a part of the loot if ItemsAdder drops it on break
        val drops = customBlock.getLoot(tool, /* includeSelfBlock = */ true)
        if (!customBlock.remove()) return false

        for (item in dropsConsumer.consume(drops)) {
            block.world.dropItemNaturally(block.location, item)
        }
        return true
    }

    private fun mimicIdOf(itemsAdderId: String): String? = ids.normalizedOf(itemsAdderId.toMimicId())
    private fun itemsAdderIdOf(blockId: String): String? = ids.nativeOf(blockId)?.let(::itemsAdderId)

    companion object {
        const val ID: String = "itemsadder"
        private const val TAG = "ItemsAdderBlockProvider"
    }
}
