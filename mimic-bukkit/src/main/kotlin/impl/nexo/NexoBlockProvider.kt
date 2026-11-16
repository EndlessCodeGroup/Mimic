// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.nexo

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.blocks.BlockProvider
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.internal.Log

/**
 * Block provider for Nexo custom blocks: note blocks, string blocks and chorus blocks. Furniture is not a block.
 * Block ID is the ID of the Nexo item placing the block, converted the same way as item IDs:
 * lowercased, with characters not allowed in block IDs replaced with `_`.
 *
 * Every variant of a directional block is listed as a separate block and can be set by its own ID,
 * but a placed variant has the ID of its parent block.
 * Nexo blocks have no states, so [setBlock] with a state always fails.
 *
 * Content of a storage block is dropped on the ground by Nexo when the block is removed,
 * so [breakBlock] doesn't pass it to the drops consumer.
 */
internal class NexoBlockProvider(
    private val nexo: NexoWrapper = NexoWrapper(),
) : BlockProvider {

    private val ids = NexoIds(nexo::hasBlock) { nexo.blockIds }

    override val id: String = ID
    override val isEnabled: Boolean get() = nexo.isEnabled

    override val allBlocks: Collection<String> get() = ids.mimicIds

    override fun hasBlock(blockId: String): Boolean = ids.findNexoId(blockId) != null

    override fun getBlockId(block: Block): String? = nexo.getBlockId(block)?.toMimicId()

    override fun getBlockIdFromItem(item: ItemStack): String? =
        nexo.getItemId(item)?.takeIf(nexo::hasBlock)?.toMimicId()

    override fun createBlockItem(blockId: String, amount: Int): ItemStack? {
        val nexoId = ids.findNexoId(blockId) ?: return null
        return nexo.createItem(nexoId, amount)
    }

    override fun setBlock(block: Block, blockId: String, state: String?): Boolean {
        val nexoId = ids.findNexoId(blockId) ?: return false
        if (state != null) {
            Log.w("[$TAG] Invalid state for block $blockId: $state")
            return false
        }

        nexo.placeBlock(block, nexoId)
        // Nexo doesn't tell if the block was placed
        return nexo.getBlockId(block) != null
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer): Boolean {
        val drops = nexo.getDrops(block, tool) ?: return false
        if (!nexo.removeBlock(block)) return false

        for (item in dropsConsumer.consume(drops)) {
            block.world.dropItemNaturally(block.location, item)
        }
        return true
    }

    companion object {
        const val ID: String = "nexo"
        private const val TAG = "NexoBlockProvider"
    }
}
