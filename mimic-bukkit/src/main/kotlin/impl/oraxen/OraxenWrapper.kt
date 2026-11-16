// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.oraxen

import io.th0rgal.oraxen.api.OraxenBlocks
import io.th0rgal.oraxen.api.OraxenItems
import io.th0rgal.oraxen.mechanics.provided.gameplay.chorusblock.ChorusBlockMechanic
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanic
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.directional.DirectionalBlock
import io.th0rgal.oraxen.mechanics.provided.gameplay.shaped.ShapedBlockMechanic
import io.th0rgal.oraxen.mechanics.provided.gameplay.stringblock.StringBlockMechanic
import io.th0rgal.oraxen.mechanics.provided.gameplay.stringblock.StringBlockMechanicFactory
import io.th0rgal.oraxen.utils.drops.Drop
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Tripwire
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.internal.OnceWarnings
import kotlin.random.Random

/**
 * Wraps Oraxen static API.
 * [getDrops] repeats the logic of Oraxen's `Drop.spawns`, because Oraxen can only drop items on the ground itself.
 */
internal class OraxenWrapper {

    val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("Oraxen")

    val itemIds: Set<String> get() = OraxenItems.getNames()

    /** IDs of all items placing a block, including the items hidden from Oraxen commands. */
    val blockIds: Set<String> get() = itemIds.filterTo(LinkedHashSet(), OraxenBlocks::isOraxenBlock)

    fun hasItem(id: String): Boolean = OraxenItems.exists(id)
    fun isBlock(id: String): Boolean = hasItem(id) && OraxenBlocks.isOraxenBlock(id)

    /** Returns ID of the [item] if this item still exists in Oraxen. */
    fun getItemId(item: ItemStack): String? = OraxenItems.getIdByItem(item)?.takeIf(OraxenItems::exists)

    fun createItem(id: String): ItemStack? = OraxenItems.getItemById(id)?.build()

    fun getBlockId(block: Block): String? {
        val mechanic = OraxenBlocks.getOraxenBlock(block.location) ?: return null
        return blockId(mechanic.itemID, (mechanic as? NoteBlockMechanic)?.directional)
    }

    /** Returns ID of the block placed by the [item], or `null` if the item places no block. */
    fun getBlockIdFromItem(item: ItemStack): String? {
        val itemId = getItemId(item)?.takeIf(OraxenBlocks::isOraxenBlock) ?: return null
        return blockId(itemId, OraxenBlocks.getNoteBlockMechanic(itemId)?.directional)
    }

    fun placeBlock(block: Block, id: String): Boolean {
        OraxenBlocks.place(id, block.location)
        return OraxenBlocks.isOraxenBlock(block)
    }

    /** Removes the [block] without drops. */
    fun removeBlock(block: Block): Boolean = OraxenBlocks.remove(block.location, null, Drop.emptyDrop())

    /** Returns items dropped by the [block] broken with the [tool], or `null` if it isn't an Oraxen block. */
    fun getDrops(block: Block, tool: ItemStack?): List<ItemStack>? {
        // Breaking rules of a block are private in Oraxen, so without a tool only the rule for an empty hand is known
        val toolOrHand = tool ?: ItemStack(Material.AIR)
        val blockData = block.blockData
        val drop = when (val mechanic = OraxenBlocks.getOraxenBlock(block.location)) {
            is NoteBlockMechanic -> mechanic.getDrop(toolOrHand)
            is ChorusBlockMechanic -> mechanic.getDrop(toolOrHand)
            is ShapedBlockMechanic -> mechanic.getDrop(toolOrHand)

            // Every level of a stackable block drops its loot
            is StringBlockMechanic -> {
                val drop = mechanic.getDrop(toolOrHand)
                val levels = (blockData as? Tripwire)
                    ?.let { mechanic.getStackDropMultiplier(StringBlockMechanicFactory.getCode(it)) } ?: 1
                Drop.clone(drop, List(levels) { drop.loots }.flatten())
            }

            else -> return null
        }

        // Double slab drops as two slabs
        val rolls = if (blockData is Slab && blockData.type == Slab.Type.DOUBLE) 2 else 1
        return List(rolls) { drop.roll(tool) }.flatten()
    }
}

/** Returns ID of the block placed by the item. Variants of a directional block have the ID of their parent block. */
internal fun blockId(itemId: String, directional: DirectionalBlock?): String = directional?.parentBlock ?: itemId

/**
 * Rolls items dropped with the [tool].
 * If the tool is `null`, requirements of the drop are treated as satisfied and no enchantments are applied.
 */
internal fun Drop.roll(tool: ItemStack?): List<ItemStack> {
    if (tool == null) return rollLoots(fortuneMultiplier = 1, tool = null)
    if (!canDrop(tool)) return emptyList()
    if (isSilktouch && tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
        return listOfNotNull(OraxenItems.getItemById(sourceID)?.build())
    }

    val fortuneLevel = if (isFortune) tool.getEnchantmentLevel(Enchantment.FORTUNE) else 0
    return rollLoots(fortuneMultiplier = 1 + if (fortuneLevel > 0) Random.nextInt(fortuneLevel) else 0, tool)
}

private fun Drop.rollLoots(fortuneMultiplier: Int, tool: ItemStack?): List<ItemStack> = loots
    .filter { Random.nextDouble() <= it.probability }
    .mapNotNull { it.getItem(fortuneMultiplier, tool) }

/** Converts Oraxen ID to Mimic ID: `Ruby Sword` -> `ruby_sword`. */
internal fun String.toMimicId(): String = lowercase().replace(notAllowedChars, "_")

/**
 * Converts the given Oraxen [ids] to Mimic IDs.
 * If two IDs become the same, the ID needing no conversion or the first one is kept,
 * the others are skipped with a warning logged once.
 */
internal fun mimicIds(ids: Collection<String>, warnings: OnceWarnings, tag: String): Collection<String> {
    val (exactIds, convertedIds) = ids.partition { it == it.toMimicId() }
    val result = LinkedHashMap<String, String>()
    for (id in exactIds + convertedIds) {
        val mimicId = id.toMimicId()
        val keptId = result.putIfAbsent(mimicId, id)
        if (keptId != null) {
            warnings.warn(id) { "[$tag] '$id' is skipped: its ID '$mimicId' is already used by '$keptId'" }
        }
    }
    return result.keys
}

private val notAllowedChars = Regex("[^a-z0-9._/-]")
