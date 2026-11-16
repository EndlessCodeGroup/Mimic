// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.blocks

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.WrappedMimicService

internal class WrappedBlockProvider(
    private val delegate: BlockProvider,
    plugin: Plugin,
) : WrappedMimicService(delegate, plugin.name), BlockProvider {

    override val allBlocks: Collection<String>
        get() = delegate.allBlocks

    override fun getBlockId(block: Block): String? = delegate.getBlockId(block)
    override fun getBlockIdFromItem(item: ItemStack): String? = delegate.getBlockIdFromItem(item)
    override fun createBlockItem(blockId: String, amount: Int): ItemStack? = delegate.createBlockItem(blockId, amount)

    override fun setBlock(block: Block, blockId: String, state: String?): Boolean =
        delegate.setBlock(block, blockId, state)

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer): Boolean =
        delegate.breakBlock(block, tool, dropsConsumer)
}
