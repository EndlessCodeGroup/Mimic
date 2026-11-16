// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.nexo

import com.nexomc.nexo.api.NexoBlocks
import com.nexomc.nexo.api.NexoItems
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic
import com.nexomc.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic
import com.nexomc.nexo.mechanics.custom_block.stringblock.stackable.StackableChild
import com.nexomc.nexo.utils.drops.Drop
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

/**
 * Wraps Nexo static API. All IDs are Nexo IDs.
 * [getDrops] repeats the logic Nexo uses to drop loot, because Nexo can only drop it on the ground.
 */
internal class NexoWrapper {

    val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("Nexo")

    val itemIds: Set<String> get() = NexoItems.itemNames()
    val blockIds: List<String> get() = NexoBlocks.blockIDs().asList()

    fun hasItem(id: String): Boolean = NexoItems.exists(id)
    fun getItemId(item: ItemStack): String? = NexoItems.idFromItem(item)?.takeIf(NexoItems::exists)

    /** Creates item with the given [amount], limited to the item's maximum stack size. */
    fun createItem(id: String, amount: Int): ItemStack? =
        NexoItems.createItem(id)?.also { it.amount = amount.coerceIn(1, it.maxStackSize) }

    fun hasBlock(id: String): Boolean = NexoBlocks.isCustomBlock(id)
    fun getBlockId(block: Block): String? = NexoBlocks.customBlockMechanic(block)?.blockId
    fun getBlockIdOfItem(itemId: String): String? = NexoBlocks.customBlockMechanic(itemId)?.blockId

    /** Places the block and returns `true` if it was placed. */
    fun placeBlock(block: Block, id: String): Boolean {
        NexoBlocks.place(id, block.location)
        // Nexo doesn't tell if the block was placed
        return NexoBlocks.isCustomBlock(block)
    }

    /** Returns items dropped by the custom [block] broken with the [tool], or `null` if it isn't a custom block. */
    fun getDrops(block: Block, tool: ItemStack?): List<ItemStack>? =
        NexoBlocks.customBlockMechanic(block)?.breakable?.drop?.rollDrops(tool, NexoItems::createItem)

    /** Removes the custom [block] without dropping its loot. */
    fun removeBlock(block: Block): Boolean = NexoBlocks.remove(block.location, null, Drop.emptyDrop())
}

/** ID of the block: variants of a directional block and levels of a stackable block have the ID of their parent block. */
internal val CustomBlockMechanic.blockId: String
    get() = when (this) {
        is NoteBlockMechanic -> directional?.parentBlock
        is StackableChild -> parentId
        else -> null
    } ?: itemID

/**
 * Returns items dropped for the [tool]. If the tool is `null`, tool requirements are satisfied
 * and the loot is rolled as for a tool without enchantments.
 */
internal fun Drop.rollDrops(tool: ItemStack?, createItem: (String) -> ItemStack?): List<ItemStack> {
    if (tool == null) return rollLoot(fortuneMultiplier = 1)
    if (!canDrop(tool)) return emptyList()

    val silkTouchItem = if (isSilkTouchDrop(tool)) createItem(sourceID) else null
    return if (silkTouchItem != null) listOf(silkTouchItem) else rollLoot(fortuneMultiplier(tool))
}

private fun Drop.rollLoot(fortuneMultiplier: Int): List<ItemStack> = loots
    .filter { Random.nextDouble() <= it.probability }
    .map { it.getItem(fortuneMultiplier) }
    .filter { it.amount > 0 }
