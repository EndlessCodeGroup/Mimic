// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mythicmobs

import io.lumine.mythic.api.items.ItemManager
import io.lumine.mythic.api.mobs.GenericCaster
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.drops.DropMetadataImpl
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Wraps MythicMobs API. All IDs are Mythic internal names of items.
 * [createItem] builds drop metadata the same way as `/mm items give`, the API has no shortcut for it.
 */
internal class MythicMobsWrapper {

    val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("MythicMobs")

    private val itemManager: ItemManager get() = MythicBukkit.inst().itemManager

    val itemIds: Collection<String> get() = itemManager.itemNames

    fun hasItem(id: String): Boolean = itemManager.getItem(id).isPresent

    /** Returns ID of the Mythic item the given [item] was generated from, if this Mythic item still exists. */
    fun getItemId(item: ItemStack): String? {
        if (item.isEmpty) return null
        return itemManager.getMythicTypeFromItem(item)?.takeIf(::hasItem)
    }

    /** Generates item with the given [amount]. The [player], if given, is the caster and the trigger of the drop. */
    fun createItem(id: String, amount: Int, player: Player?): ItemStack? {
        val mythicItem = itemManager.getItem(id).orElse(null) ?: return null
        val item = if (player != null) {
            val mythicPlayer = BukkitAdapter.adapt(player)
            mythicItem.generateItemStack(DropMetadataImpl(GenericCaster(mythicPlayer), mythicPlayer), amount)
        } else {
            mythicItem.generateItemStack(amount)
        }
        return BukkitAdapter.adapt(item)
    }
}
