// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.vanilla

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Campfire
import org.bukkit.block.Chest
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.BlockInventoryHolder
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.blocks.BlockProvider
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.internal.Log

/**
 * Block provider using material name as block ID.
 * Supports block state properties in the vanilla format in [setBlock], for example `[facing=north]`.
 *
 * Contents of blocks holding items, like chests or campfires, are passed to the drops consumer in [breakBlock].
 * Shulker boxes keep their contents inside the dropped item.
 */
internal class MinecraftBlockProvider : BlockProvider {

    override val id: String = ID

    override val allBlocks: Set<String> by lazy {
        blockMaterials.mapTo(LinkedHashSet()) { it.name.lowercase() }
    }

    /** Maps items to blocks placed by these items, for example `wheat_seeds` -> `wheat`. */
    private val blocksByItem: Map<Material, Material> by lazy {
        blockMaterials
            .groupBy { it.createBlockData().placementMaterial }
            .filterKeys { !it.isAir }
            .mapValues { (item, blocks) -> blocks.firstOrNull { it == item } ?: blocks.first() }
    }

    private val blockMaterials: List<Material>
        get() = Material.entries.filter { it.isBlock && !it.isLegacy }

    override fun getBlockId(block: Block): String = block.type.name.lowercase()

    override fun getBlockIdFromItem(item: ItemStack): String? {
        if (item.isEmpty) return null
        return blocksByItem[item.type]?.name?.lowercase()
    }

    override fun createBlockItem(blockId: String, amount: Int): ItemStack? {
        val item = getMaterial(blockId)?.createBlockData()?.placementMaterial ?: return null
        if (!item.isItem || item.isAir) return null
        return ItemStack(item, amount)
    }

    override fun setBlock(block: Block, blockId: String, state: String?): Boolean {
        val material = getMaterial(blockId) ?: return false

        if (state == null) {
            block.type = material
            return true
        }

        block.blockData = try {
            Bukkit.createBlockData(material, state)
        } catch (cause: IllegalArgumentException) {
            Log.w(cause, "[$TAG] Invalid state for block $blockId: $state")
            return false
        }
        return true
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer): Boolean {
        if (block.isEmpty) return false

        val drops = block.getDrops(tool) + takeContents(block)
        block.type = Material.AIR

        for (item in dropsConsumer.consume(drops)) {
            block.world.dropItemNaturally(block.location, item)
        }
        return true
    }

    /** Takes items held by the [block], so they will not be dropped when the block is removed. */
    private fun takeContents(block: Block): List<ItemStack> = when (val state = block.getState(false)) {
        is ShulkerBox -> emptyList()
        is BlockInventoryHolder -> {
            // Take only this half of a double chest
            val inventory = if (state is Chest) state.blockInventory else state.inventory
            val contents = inventory.contents.filterNotNull().filterNot { it.isEmpty }
            inventory.clear()
            contents
        }

        is Campfire -> (0 until state.size).mapNotNull { slot ->
            state.getItem(slot)
                ?.takeUnless { it.isEmpty }
                .also { state.setItem(slot, null) }
        }

        else -> emptyList()
    }

    private fun getMaterial(name: String): Material? =
        Material.getMaterial(name.uppercase())?.takeIf { it.isBlock && !it.isLegacy }

    companion object {
        const val ID: String = "minecraft"
        private const val TAG = "MinecraftBlockProvider"
    }
}
