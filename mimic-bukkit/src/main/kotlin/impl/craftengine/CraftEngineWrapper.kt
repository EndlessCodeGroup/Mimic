// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.craftengine

import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks
import net.momirealms.craftengine.bukkit.api.CraftEngineItems
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition
import net.momirealms.craftengine.bukkit.item.BukkitItemManager
import net.momirealms.craftengine.bukkit.util.BlockStateUtils
import net.momirealms.craftengine.core.block.BlockDefinition
import net.momirealms.craftengine.core.block.ImmutableBlockState
import net.momirealms.craftengine.core.block.parser.BlockStateParser
import net.momirealms.craftengine.core.item.ItemDefinition
import net.momirealms.craftengine.core.util.Key
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

/**
 * Wraps CraftEngine static API.
 * [parseBlockState] and [isCorrectTool] use CraftEngine internals, not its public API.
 */
internal class CraftEngineWrapper {

    val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("CraftEngine")

    /** Custom items, excluding vanilla items modified by CraftEngine. */
    val loadedCustomItems: List<ItemDefinition>
        get() = CraftEngineItems.loadedItems().values.filterNot { it.isVanillaItem }
    val loadedBlockIds: Collection<Key> get() = CraftEngineBlocks.loadedBlocks().keys

    fun getCustomItem(id: Key): BukkitItemDefinition? = CraftEngineItems.byId(id)?.takeUnless { it.isVanillaItem }
    fun getCustomItem(item: ItemStack): BukkitItemDefinition? =
        CraftEngineItems.byItemStack(item)?.takeUnless { it.isVanillaItem }

    /** Creates custom item with the given [amount], limited to the item's maximum stack size. */
    fun createCustomItem(id: Key, amount: Int): ItemStack? =
        getCustomItem(id)?.buildBukkitItem()?.also { it.amount = amount.coerceIn(1, it.maxStackSize) }

    fun getBlock(id: Key): BlockDefinition? = CraftEngineBlocks.byId(id)
    fun getBlockState(block: Block): ImmutableBlockState? = CraftEngineBlocks.getCustomBlockState(block)
    /** Returns `null` if the [state] is invalid. CraftEngine's parser throws on malformed input like `id[`. */
    fun parseBlockState(state: String): ImmutableBlockState? = try {
        BlockStateParser.deserialize(state)
    } catch (_: IndexOutOfBoundsException) {
        null
    }
    fun isCorrectTool(state: ImmutableBlockState, tool: ItemStack): Boolean =
        BlockStateUtils.isCorrectTool(state, BukkitItemManager.instance().wrap(tool))

    fun placeBlock(location: Location, state: ImmutableBlockState): Boolean =
        CraftEngineBlocks.place(location, state, /* playSound = */ false)

    fun removeBlock(block: Block): Boolean = CraftEngineBlocks.remove(block)
}

/** Converts CraftEngine key to Mimic ID: `default:palm_log` -> `default/palm_log`. */
internal fun Key.toMimicId(): String = "${namespace()}/${value()}"

/** Converts Mimic ID to CraftEngine key: `default/palm_log` -> `default:palm_log`. */
internal fun craftEngineKey(id: String): Key? {
    val namespace = id.substringBefore('/', missingDelimiterValue = "")
    if (namespace.isEmpty()) return null
    return Key.of(namespace, id.substringAfter('/'))
}
