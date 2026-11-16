// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.nexo

import com.nexomc.nexo.api.NexoBlocks
import com.nexomc.nexo.api.NexoItems
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic
import com.nexomc.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic
import com.nexomc.nexo.utils.drops.Drop
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

/**
 * Wraps Nexo static API.
 * [getDrops] repeats the logic Nexo uses to drop loot, because Nexo can only drop it on the ground.
 */
internal class NexoWrapper {

    val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("Nexo")

    val itemIds: List<String> get() = NexoItems.itemNames().toList()
    val blockIds: List<String> get() = NexoBlocks.blockIDs().asList()

    fun hasItem(id: String): Boolean = NexoItems.exists(id)
    fun getItemId(item: ItemStack): String? = NexoItems.idFromItem(item)?.takeIf(NexoItems::exists)

    /** Creates item with the given [amount], limited to the item's maximum stack size. */
    fun createItem(id: String, amount: Int): ItemStack? =
        NexoItems.createItem(id)?.also { it.amount = amount.coerceIn(1, it.maxStackSize) }

    fun hasBlock(id: String): Boolean = NexoBlocks.isCustomBlock(id)
    fun getBlockId(block: Block): String? = NexoBlocks.customBlockMechanic(block)?.itemID

    fun placeBlock(block: Block, id: String) {
        NexoBlocks.place(id, block.location)
    }

    /**
     * Returns items dropped by the custom [block] broken with the [tool], or `null` if it isn't a custom block.
     * If the tool is `null`, the block drops its loot regardless of the required tool.
     */
    fun getDrops(block: Block, tool: ItemStack?): List<ItemStack>? {
        val drop = NexoBlocks.customBlockMechanic(block)?.drop ?: return null

        if (tool == null) return drop.rollLoot(fortuneMultiplier = 1)
        if (!drop.canDrop(tool)) return emptyList()

        val silkTouchItem = if (drop.isSilkTouchDrop(tool)) NexoItems.createItem(drop.sourceID) else null
        return if (silkTouchItem != null) listOf(silkTouchItem) else drop.rollLoot(drop.fortuneMultiplier(tool))
    }

    /** Removes the custom [block] without dropping its loot. */
    fun removeBlock(block: Block): Boolean = NexoBlocks.remove(block.location, null, Drop.emptyDrop())

    /** Directional blocks take the drop from their parent block. */
    private val CustomBlockMechanic.drop: Drop
        get() {
            val mechanic = (this as? NoteBlockMechanic)?.directional?.parentMechanic ?: this
            return mechanic.breakable.drop
        }

    private fun Drop.rollLoot(fortuneMultiplier: Int): List<ItemStack> = loots
        .filter { Random.nextDouble() <= it.probability }
        .map { it.getItem(fortuneMultiplier) }
        .filter { it.amount > 0 }
}
