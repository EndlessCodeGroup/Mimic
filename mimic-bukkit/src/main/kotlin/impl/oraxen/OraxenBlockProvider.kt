// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.oraxen

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.blocks.BlockProvider
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.internal.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Block provider for Oraxen custom blocks. Furniture is not a block.
 * ID of a block is the ID of the Oraxen item placing it, converted in the same way as in [OraxenItemProvider].
 * Variants of a directional block have the ID of their parent block.
 * Block states are not supported in [setBlock], every state is invalid.
 *
 * Drops in [breakBlock] are rolled from the Oraxen drop configured for the tool.
 * Without a tool, requirements of the drop are treated as satisfied,
 * but the drop is the one configured for an empty hand:
 * Oraxen has no API to select a breaking rule requiring a tool without this tool.
 *
 * Contents of Oraxen storage blocks are dropped on the ground by Oraxen when the block is removed,
 * so [breakBlock] doesn't pass them to the drops consumer.
 * Oraxen has no API to take these items before removal.
 */
internal class OraxenBlockProvider(
    private val oraxen: OraxenWrapper = OraxenWrapper(),
) : BlockProvider {

    private val skippedIds = ConcurrentHashMap.newKeySet<String>()

    override val id: String = ID
    override val isEnabled: Boolean get() = oraxen.isEnabled

    override val allBlocks: Collection<String>
        get() = blocksByMimicId().keys

    override fun hasBlock(blockId: String): Boolean = findBlock(blockId) != null

    override fun getBlockId(block: Block): String? = oraxen.getBlockId(block)?.toMimicId()

    override fun getBlockIdFromItem(item: ItemStack): String? =
        oraxen.getItemId(item)?.takeIf(oraxen::isBlock)?.toMimicId()

    override fun createBlockItem(blockId: String, amount: Int): ItemStack? {
        val oraxenId = findBlock(blockId) ?: return null
        return oraxen.createItem(oraxenId)?.also { it.amount = amount }
    }

    override fun setBlock(block: Block, blockId: String, state: String?): Boolean {
        val oraxenId = findBlock(blockId) ?: return false
        if (state != null) {
            Log.w("[$TAG] Invalid state for block $blockId: $state")
            return false
        }
        return oraxen.placeBlock(block, oraxenId)
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer): Boolean {
        val drops = oraxen.getDrops(block, tool) ?: return false
        if (!oraxen.removeBlock(block)) return false

        for (item in dropsConsumer.consume(drops)) {
            block.world.dropItemNaturally(block.location, item)
        }
        return true
    }

    private fun findBlock(blockId: String): String? =
        if (oraxen.isBlock(blockId)) blockId else blocksByMimicId()[blockId]

    private fun blocksByMimicId(): Map<String, String> = mimicIdsMap(oraxen.blockIds, skippedIds, TAG)

    companion object {
        const val ID: String = "oraxen"
        private const val TAG = "OraxenBlockProvider"
    }
}
