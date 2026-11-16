// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.craftengine

import net.momirealms.craftengine.core.block.BlockDefinition
import net.momirealms.craftengine.core.block.ImmutableBlockState
import net.momirealms.craftengine.core.item.behavior.BlockItem
import net.momirealms.craftengine.core.util.Key
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.blocks.BlockProvider
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.internal.Log

/**
 * Block provider for CraftEngine custom blocks.
 * CraftEngine namespace becomes the first path segment of block ID: `default:palm_log` -> `default/palm_log`.
 * Supports block state properties in [setBlock], for example `[facing=north]`.
 *
 * Items held by CraftEngine block entities, like storage contents or an item in a frame,
 * are dropped on the ground by CraftEngine when the block is removed, so [breakBlock] doesn't pass them to the drops consumer.
 * CraftEngine has no API to take these items before removal.
 */
internal class CraftEngineBlockProvider private constructor(
    private val craftEngine: CraftEngineWrapper,
) : BlockProvider {

    constructor() : this(CraftEngineWrapper())

    override val id: String = ID
    override val isEnabled: Boolean get() = craftEngine.isEnabled

    override val allBlocks: Collection<String>
        get() = craftEngine.loadedBlockIds.map(Key::toMimicId)

    override fun hasBlock(blockId: String): Boolean = findBlock(blockId) != null

    override fun getBlockId(block: Block): String? {
        val state = getCustomBlockState(block) ?: return null
        return state.owner().value()?.id()?.toMimicId()
    }

    override fun getBlockIdFromItem(item: ItemStack): String? {
        val customItem = craftEngine.getCustomItem(item) ?: return null
        return customItem.behavior().getFirst(BlockItem::class.java)?.block()?.toMimicId()
    }

    override fun createBlockItem(blockId: String, amount: Int): ItemStack? {
        val itemId = findBlock(blockId)?.defaultState()?.settings()?.itemId() ?: return null
        return craftEngine.createCustomItem(itemId, amount)
    }

    override fun setBlock(block: Block, blockId: String, state: String?): Boolean {
        val definition = findBlock(blockId) ?: return false

        if (state == null) return craftEngine.placeBlock(block.location, definition.defaultState())

        // State not starting with '[' could continue the ID, so a state of another block is rejected
        val blockState = craftEngine.parseBlockState(definition.id().asString() + state)
            ?.takeIf { it.owner().value() == definition }
        if (blockState == null) {
            Log.w("[$TAG] Invalid state for block $blockId: $state")
            return false
        }
        return craftEngine.placeBlock(block.location, blockState)
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer): Boolean {
        val state = getCustomBlockState(block) ?: return false

        // CraftEngine applies its loot table to vanilla drops, but checks the tool only if the block is broken by an entity
        val isCorrectTool = tool == null || craftEngine.isCorrectTool(state, tool)
        val drops = if (isCorrectTool) block.getDrops(tool) else emptyList()
        craftEngine.removeBlock(block)

        for (item in dropsConsumer.consume(drops)) {
            block.world.dropItemNaturally(block.location, item)
        }
        return true
    }

    private fun getCustomBlockState(block: Block): ImmutableBlockState? =
        craftEngine.getBlockState(block)?.takeUnless { it.isEmpty }

    private fun findBlock(blockId: String): BlockDefinition? = craftEngineKey(blockId)?.let(craftEngine::getBlock)

    companion object {
        const val ID: String = "craftengine"
        private const val TAG = "CraftEngineBlockProvider"
    }
}
