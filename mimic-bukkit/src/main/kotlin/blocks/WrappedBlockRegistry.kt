// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.blocks

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.WrappedMimicService
import ru.endlesscode.mimic.internal.Log

@ExperimentalMimicApi
internal class WrappedBlockRegistry(
    private val delegate: BukkitBlockRegistry,
    pluginName: String,
) : WrappedMimicService(delegate, pluginName), BukkitBlockRegistry {

    constructor(delegate: BukkitBlockRegistry, plugin: Plugin) : this(delegate, plugin.name)

    override val knownIds: Collection<String> by delegate::knownIds

    override fun isSameBlock(block: Block, blockId: String) = delegate.isSameBlock(block, blockId)

    override fun isSameBlockItem(blockItem: ItemStack, blockId: String) = delegate.isSameBlockItem(blockItem, blockId)

    override fun isBlockExists(blockId: String) = delegate.isBlockExists(blockId)

    override fun getBlockId(block: Block) = delegate.getBlockId(block)

    override fun getBlockIdFromItem(blockItem: ItemStack) = delegate.getBlockIdFromItem(blockItem)

    override fun getBlockItem(blockId: String, amount: Int) = delegate.getBlockItem(blockId, amount)

    override fun setBlock(block: Block, blockId: String, payload: Any?) = delegate.setBlock(block, blockId, payload)

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer<ItemStack>) =
        delegate.breakBlock(block, tool, dropsConsumer.dropAllOnFailure())
}

/** Drops all items on the ground if the consumer fails, so they don't disappear when the block is already removed. */
@ExperimentalMimicApi
private fun DropsConsumer<ItemStack>.dropAllOnFailure() = DropsConsumer { drops ->
    try {
        consume(drops)
    } catch (e: Throwable) {
        Log.w(e, "Drops consumer failed, dropping all items on the ground")
        drops
    }
}
